package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.marketdata.application.port.out.CoinQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.CoinSymbolPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CoinSymbolAdapter implements CoinSymbolPort {

    private final CoinQueryPort coinQueryPort;

    @Override
    public Map<Long, String> findSymbolsByIds(Set<Long> coinIds) {
        if (coinIds.isEmpty()) {
            return Map.of();
        }
        return coinQueryPort.findSymbolsByIds(coinIds);
    }

    @Override
    public Map<Long, String> findSymbolsByExchangeCoinIds(Set<Long> exchangeCoinIds) {
        if (exchangeCoinIds.isEmpty()) {
            return Map.of();
        }
        return coinQueryPort.findSymbolsByExchangeCoinIds(exchangeCoinIds);
    }

    @Override
    public Map<Long, Long> findCoinIdsByExchangeCoinIds(Set<Long> exchangeCoinIds) {
        if (exchangeCoinIds.isEmpty()) {
            return Map.of();
        }
        return coinQueryPort.findCoinIdsByExchangeCoinIds(exchangeCoinIds);
    }
}
