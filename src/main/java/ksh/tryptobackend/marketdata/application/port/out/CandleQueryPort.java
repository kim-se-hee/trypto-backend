package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.Candle;
import ksh.tryptobackend.marketdata.domain.model.CandleInterval;

import java.time.Instant;
import java.util.List;

public interface CandleQueryPort {

    List<Candle> findByExchangeAndCoinAndInterval(
        String exchange, String coin, CandleInterval interval, int limit, Instant cursor);
}
