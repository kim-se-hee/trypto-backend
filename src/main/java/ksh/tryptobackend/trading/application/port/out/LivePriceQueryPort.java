package ksh.tryptobackend.trading.application.port.out;

import java.math.BigDecimal;

public interface LivePriceQueryPort {

    BigDecimal getCurrentPrice(Long exchangeCoinId);
}
