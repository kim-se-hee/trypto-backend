거래소 시세 정규화 결과를 실시간 브로드캐스트하는 채널.

# 채널

| 항목 | 값 |
|------|------|
| 종류 | RabbitMQ Fanout Exchange |
| 이름 | `ticker.exchange` (`app.rabbitmq.ticker-exchange`로 외부화) |
| 발행자 | `collector` — `ksh.tryptocollector.rabbitmq.TickerEventPublisher` |
| 소비자 | `api` — `ksh.tryptobackend.marketdata.adapter.in.LiveTickerEventListener` |
| Content-Type | `application/json` |
| Routing key | `""` (fanout) |
| Durable | exchange durable, 소비자 큐는 비-durable / exclusive / auto-delete |

# 페이로드

```json
{
  "exchange":      "UPBIT",
  "symbol":        "BTC/KRW",
  "currentPrice":  "152340000",
  "changeRate":    "0.0123",
  "quoteTurnover": "8423199301.55",
  "timestamp":     1735689600123
}
```

| 필드 | 약속 |
|------|------|
| `exchange` | `UPBIT` / `BITHUMB` / `BINANCE` |
| `symbol` | `{base}/{quote}` (예: `BTC/KRW`, `ETH/USDT`) |
| `currentPrice` | quote 통화 단위 |
| `changeRate` | 24h 변동률. 소수점 (1% = `0.01`) |
| `quoteTurnover` | quote 통화 단위 24h 거래대금 |
| `timestamp` | epoch **milliseconds** |
