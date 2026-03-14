package ksh.tryptobackend.marketdata.domain.model;

import java.time.Instant;

public record Candle(
    Instant time,
    double open,
    double high,
    double low,
    double close
) {
}
