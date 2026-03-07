package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.ranking.application.port.out.ExchangeSnapshotQueryPort;
import ksh.tryptobackend.ranking.domain.vo.ExchangeSnapshot;
import ksh.tryptobackend.ranking.domain.vo.KrwConversionRate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockExchangeInfoQueryAdapter implements ExchangeSnapshotQueryPort {

    private final Map<Long, ExchangeSnapshot> exchanges = new ConcurrentHashMap<>();

    @Override
    public ExchangeSnapshot getExchangeInfo(Long exchangeId) {
        return exchanges.get(exchangeId);
    }

    public void addExchange(Long exchangeId, Long baseCurrencyCoinId, KrwConversionRate conversionRate) {
        exchanges.put(exchangeId, new ExchangeSnapshot(exchangeId, baseCurrencyCoinId, conversionRate));
    }

    public void clear() {
        exchanges.clear();
    }
}
