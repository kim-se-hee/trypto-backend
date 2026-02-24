package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.trading.application.port.out.TradingVenuePort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockTradingVenueAdapter implements TradingVenuePort {

    private final Map<Long, TradingVenue> venues = new ConcurrentHashMap<>();

    @Override
    public Optional<TradingVenue> findByExchangeId(Long exchangeId) {
        return Optional.ofNullable(venues.get(exchangeId));
    }

    public void addVenue(TradingVenue data) {
        venues.put(data.exchangeId(), data);
    }

    public void clear() {
        venues.clear();
    }
}
