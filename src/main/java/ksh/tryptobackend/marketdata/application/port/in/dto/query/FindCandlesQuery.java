package ksh.tryptobackend.marketdata.application.port.in.dto.query;

import ksh.tryptobackend.marketdata.domain.model.CandleInterval;

import java.time.Instant;

public record FindCandlesQuery(
    String exchange,
    String coin,
    CandleInterval interval,
    int limit,
    Instant cursor
) {
}
