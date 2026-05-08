// trypto 시세 STOMP/WebSocket 부하 테스트 — 단일 ramp(up/sustain/down).
//
// 시나리오 시간선:
//   T=0~10분  : 동접 0 → 5,000 / 시세 0 → 241/s   (선형 ramp up, 비율 5000:241 동조)
//   T=10~20분 : 동접 5,000 / 시세 241/s            (sustain)
//   T=20~25분 : 동접 5,000 → 0 / 시세 241 → 0/s   (선형 ramp down)
//
// 거래소 분포 (VU 입주):
//   Upbit   90% → /topic/tickers.1
//   Bithumb  5% → /topic/tickers.2
//   Binance  5% → /topic/tickers.3
//
// 측정값 (k6 결과):
//   message_e2e_latency      : 서버 timestamp 박힘 → 클라 도착 시각 (ms)
//   stomp_disconnects        : VU 연결 끊김 누계 (heartbeat drift 감지용)
//   stomp_connect_failures   : 초기 연결 실패 누계
//   stomp_messages_received  : 받은 STOMP MESSAGE 누계 (부하 검증용)

import http from 'k6/http';
import { check, fail } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { WebSocket } from 'k6/experimental/websockets';
import { setInterval, clearInterval, setTimeout } from 'k6/timers';
import {
  buildConnectFrame,
  buildSubscribeFrame,
  buildDisconnectFrame,
  parseFrames,
  defaultConnectOptions,
  HEARTBEAT_FRAME,
} from '../lib/stomp.js';

const e2eLatency = new Trend('message_e2e_latency', true);
const disconnects = new Counter('stomp_disconnects');
const connectFailures = new Counter('stomp_connect_failures');
const messagesReceived = new Counter('stomp_messages_received');

const API_HOST = __ENV.API_HOST || 'localhost:8080';
const COLLECTOR_HOST = __ENV.COLLECTOR_HOST || 'localhost:8081';
const WS_URL = `ws://${API_HOST}/ws`;

const TARGET_VU = parseInt(__ENV.TARGET_VU || '5000', 10);
const PEAK_RATE_UPBIT = 217;
const PEAK_RATE_BITHUMB = 12;
const PEAK_RATE_BINANCE = 12;

const RAMP_UP_DURATION = '10m';
const SUSTAIN_DURATION = '10m';
const RAMP_DOWN_DURATION = '5m';

export const options = {
  scenarios: {
    ticker_websocket: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: RAMP_UP_DURATION, target: TARGET_VU },
        { duration: SUSTAIN_DURATION, target: TARGET_VU },
        { duration: RAMP_DOWN_DURATION, target: 0 },
      ],
      gracefulRampDown: '30s',
      gracefulStop: '30s',
      exec: 'subscribeAndIdle',
    },
  },
  thresholds: {
    'message_e2e_latency':   ['p(95)<200', 'p(99)<500'],
    'stomp_disconnects':     ['count<50'],
    'stomp_connect_failures':['count<50'],
  },
};

// k6 setup() 은 테스트 시작 시 한 번 호출. collector 의 ramp 도 같은 모양으로 시작시킨다.
// VU 의 ramp up 과 시세 발행 ramp 이 같은 시각에 출발하므로 비율 5000:241 이 자연스럽게 유지된다.
export function setup() {
  const profile = {
    phases: [
      {
        durationSeconds: durationToSeconds(RAMP_UP_DURATION),
        toRates: { UPBIT: PEAK_RATE_UPBIT, BITHUMB: PEAK_RATE_BITHUMB, BINANCE: PEAK_RATE_BINANCE },
      },
      {
        durationSeconds: durationToSeconds(SUSTAIN_DURATION),
        toRates: { UPBIT: PEAK_RATE_UPBIT, BITHUMB: PEAK_RATE_BITHUMB, BINANCE: PEAK_RATE_BINANCE },
      },
      {
        durationSeconds: durationToSeconds(RAMP_DOWN_DURATION),
        toRates: { UPBIT: 0, BITHUMB: 0, BINANCE: 0 },
      },
    ],
  };
  const res = http.post(
    `http://${COLLECTOR_HOST}/loadtest/ticker/ramp`,
    JSON.stringify(profile),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(res, { 'collector ramp 시작 200': (r) => r.status === 200 }) || fail('collector ramp 시작 실패');
  return { startedAt: Date.now() };
}

export function teardown() {
  http.post(`http://${COLLECTOR_HOST}/loadtest/ticker/stop`);
}

export function subscribeAndIdle() {
  const exchangeId = pickExchangeId();
  const destination = `/topic/tickers.${exchangeId}`;

  const ws = new WebSocket(WS_URL);
  let connected = false;
  let heartbeatTimer = null;

  ws.onopen = () => {
    const opts = defaultConnectOptions(API_HOST);
    ws.send(buildConnectFrame(opts.host, opts.heartbeatSendMs, opts.heartbeatRecvMs));
  };

  ws.onmessage = (evt) => {
    const text = typeof evt.data === 'string' ? evt.data : '';
    if (!text) return;

    const frames = parseFrames(text);
    for (const frame of frames) {
      if (frame.command === 'CONNECTED') {
        connected = true;
        // CONNECTED 받은 직후 SUBSCRIBE
        ws.send(buildSubscribeFrame(destination, `sub-${__VU}`));
        // 클라→서버 하트비트 시작 (CONNECTED 후 시점부터)
        heartbeatTimer = setInterval(() => {
          try { ws.send(HEARTBEAT_FRAME); } catch (_) { /* close 직후 send 는 무시 */ }
        }, defaultConnectOptions().heartbeatSendMs);
      } else if (frame.command === 'MESSAGE') {
        recordLatency(frame.body);
        messagesReceived.add(1);
      } else if (frame.command === 'ERROR') {
        // STOMP ERROR — 서버가 끊을 예정. 카운트만.
      }
    }
  };

  ws.onclose = () => {
    if (heartbeatTimer !== null) {
      clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    }
    if (connected) {
      // 정상 sustain 중 끊김 = heartbeat drift 또는 서버 강제 종료 의심
      disconnects.add(1);
    } else {
      connectFailures.add(1);
    }
  };

  ws.onerror = () => {
    if (!connected) {
      connectFailures.add(1);
    }
  };

  // VU 가 ramping-vus executor 의 요청에 의해 stop 되면 자동으로 close 한다.
  // 명시적으로 sleep 하지 않아도 onmessage 가 비동기로 메시지를 받는다.
  // 단, k6 의 VU 함수는 return 하면 다음 iteration 으로 넘어가므로
  // 끝까지 살아있게 하려면 wait 한다.
  return new Promise((resolve) => {
    // VU lifecycle 관리: 30분 + 마진 후 자체 종료
    setTimeout(() => {
      try {
        ws.send(buildDisconnectFrame());
      } catch (_) { /* ignore */ }
      try { ws.close(); } catch (_) { /* ignore */ }
      resolve();
    }, 30 * 60 * 1000);
  });
}

function recordLatency(body) {
  if (!body) return;
  try {
    const obj = JSON.parse(body);
    if (typeof obj.timestamp === 'number') {
      const lat = Date.now() - obj.timestamp;
      if (lat >= 0 && lat < 60_000) {
        // 음수/거대값 = 시계 어긋남이거나 garbage. 트림.
        e2eLatency.add(lat);
      }
    }
  } catch (_) { /* JSON 파싱 실패는 카운트하지 않음 */ }
}

function pickExchangeId() {
  const dice = Math.random();
  if (dice < 0.90) return 1;        // UPBIT
  if (dice < 0.95) return 2;        // BITHUMB
  return 3;                         // BINANCE
}

function durationToSeconds(s) {
  const m = /^(\d+)([smh])$/.exec(s);
  if (!m) throw new Error(`bad duration: ${s}`);
  const n = parseInt(m[1], 10);
  switch (m[2]) {
    case 's': return n;
    case 'm': return n * 60;
    case 'h': return n * 3600;
  }
}
