package ksh.tryptobackend.marketdata.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindCandlesUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.query.FindCandlesQuery;
import ksh.tryptobackend.marketdata.application.port.out.CandleQueryPort;
import ksh.tryptobackend.marketdata.domain.model.Candle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindCandlesService implements FindCandlesUseCase {

    private final CandleQueryPort candleQueryPort;

    @Override
    public List<Candle> findCandles(FindCandlesQuery query) {
        return candleQueryPort.findByExchangeAndCoinAndInterval(
            query.exchange(), query.coin(), query.interval(), query.limit(), query.cursor());
    }
}
