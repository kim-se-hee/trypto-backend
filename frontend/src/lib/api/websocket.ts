import { Client, type StompSubscription } from "@stomp/stompjs";
import { API_BASE_URL } from "./client";

export interface Ticker {
  coinId: number;
  symbol: string;
  price: number;
  changeRate: number;
  quoteTurnover: number;
  timestamp: number;
}

export interface UserEvent {
  eventType: string;
  walletId: number;
  orderId: number;
  coinId: number;
  side: string;
  quantity: number;
  price: number;
  fee: number;
}

const WS_BASE_URL = (import.meta.env.VITE_WS_BASE_URL as string | undefined) ?? "";

function resolveWsUrl(): string {
  if (WS_BASE_URL) return WS_BASE_URL;

  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}/ws`;
}

interface Subscriber {
  topic: string;
  handler: (body: string) => void;
  subscription: StompSubscription | null;
}

// 시세는 SSE 로 분리됐고 이 STOMP 클라이언트는 체결 통지(/user/{userId}/queue/events) 전용이다.
let client: Client | null = null;
let visibilityListenerAttached = false;
let hiddenAt: number | null = null;
const subscribers = new Set<Subscriber>();

function activateSubscriber(sub: Subscriber): void {
  if (!client?.connected) return;
  if (sub.subscription) return;
  sub.subscription = client.subscribe(sub.topic, (message) => {
    sub.handler(message.body);
  });
}

function invalidateSubscriptions(): void {
  subscribers.forEach((sub) => {
    sub.subscription = null;
  });
}

function reactivateAllSubscribers(): void {
  subscribers.forEach(activateSubscriber);
}

function forceReconnect(): void {
  if (!client) return;
  // 백오프가 길게 누적된 상태일 수 있으므로 즉시 재시도하도록 짧게 강제
  client.reconnectDelay = 100;
  void client.forceDisconnect();
}

function handleVisibilityChange(): void {
  if (!client) return;
  if (document.visibilityState === "hidden") {
    hiddenAt = Date.now();
    return;
  }
  const elapsed = hiddenAt ? Date.now() - hiddenAt : 0;
  hiddenAt = null;
  // heartbeat 간격(10s)의 2배 넘게 백그라운드였다면 서버가 이미 세션을 끊었을 가능성이 높다
  // client.connected 만 보면 ERROR 프레임 처리 전이라 zombie 를 못 잡으므로 시간 기반으로 강제 재연결
  if (elapsed > 20000 || !client.connected) {
    if (client.active) {
      forceReconnect();
    } else {
      client.activate();
    }
  }
}

export function connect(): void {
  if (client?.active) return;

  client = new Client({
    brokerURL: resolveWsUrl(),
    reconnectDelay: 1000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    connectionTimeout: 5000,
  });

  let reconnectAttempts = 0;
  const originalReconnectDelay = client.reconnectDelay;

  client.onWebSocketClose = () => {
    invalidateSubscriptions();
    reconnectAttempts++;
    const delay = Math.min(originalReconnectDelay * Math.pow(2, reconnectAttempts), 30000);
    if (client) {
      client.reconnectDelay = delay;
    }
  };

  client.onConnect = () => {
    reconnectAttempts = 0;
    if (client) {
      client.reconnectDelay = originalReconnectDelay;
    }
    reactivateAllSubscribers();
  };

  client.onStompError = (frame) => {
    console.error("STOMP error:", frame.headers.message);
  };

  client.activate();

  if (!visibilityListenerAttached) {
    document.addEventListener("visibilitychange", handleVisibilityChange);
    visibilityListenerAttached = true;
  }
}

export function disconnect(): void {
  if (client?.active) {
    void client.deactivate();
  }
  client = null;
  subscribers.clear();
}

/**
 * 거래소 1개의 시세를 SSE 로 구독한다.
 *
 * 거래소 탭을 바꾸면 호출 측이 cleanup 함수를 호출해 EventSource 를 close 하고 새 거래소를 구독한다.
 * 한 클라이언트가 동시에 여러 거래소를 구독하지 않는다.
 */
export function subscribeTickers(
  exchangeId: number,
  callback: (tickers: Ticker[]) => void,
): () => void {
  const url = `${API_BASE_URL}/api/sse/tickers/${exchangeId}`;
  const source = new EventSource(url);

  source.onmessage = (event) => {
    try {
      const parsed = JSON.parse(event.data) as Ticker[];
      if (Array.isArray(parsed) && parsed.length > 0) {
        callback(parsed);
      }
    } catch {
      // ignore parse errors
    }
  };

  // EventSource 는 연결 끊김 시 브라우저가 자동 재연결한다. 로깅만 한다.
  source.onerror = () => {
    if (source.readyState === EventSource.CLOSED) {
      // 명시적으로 닫힌 상태는 cleanup 경로 — 별다른 처리 없음
    }
  };

  return () => {
    source.close();
  };
}

export function subscribeUserEvents(
  userId: number,
  callback: (event: UserEvent) => void,
): () => void {
  const sub: Subscriber = {
    topic: `/user/${userId}/queue/events`,
    handler: (body) => {
      try {
        callback(JSON.parse(body) as UserEvent);
      } catch {
        // ignore parse errors
      }
    },
    subscription: null,
  };
  subscribers.add(sub);
  activateSubscriber(sub);
  return () => {
    subscribers.delete(sub);
    sub.subscription?.unsubscribe();
    sub.subscription = null;
  };
}

export function isConnected(): boolean {
  return client?.active ?? false;
}
