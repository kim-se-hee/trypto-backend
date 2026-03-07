package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.ranking.application.port.out.ExchangeCoinQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("rankingExchangeCoinQueryAdapter")
@RequiredArgsConstructor
public class ExchangeCoinQueryAdapter implements ExchangeCoinQueryPort {

    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;

    @Override
    public Optional<Long> findExchangeCoinId(Long exchangeId, Long coinId) {
        return findExchangeCoinMappingUseCase.findExchangeCoinId(exchangeId, coinId);
    }

    @Override
    public Map<Long, Long> findExchangeCoinIdsByExchangeIdAndCoinIds(Long exchangeId, List<Long> coinIds) {
        return findExchangeCoinMappingUseCase.findExchangeCoinIdMap(exchangeId, coinIds);
    }
}
