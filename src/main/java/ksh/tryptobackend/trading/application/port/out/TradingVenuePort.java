package ksh.tryptobackend.trading.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

public interface TradingVenuePort {

    Optional<TradingVenue> findByExchangeId(Long exchangeId);

    record TradingVenue(
        Long exchangeId,
        BigDecimal feeRate,
        Long baseCurrencyCoinId,
        String baseCurrencySymbol
    ) {
    }
}
