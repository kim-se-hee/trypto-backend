package ksh.tryptobackend.ranking.application.port.out;

import java.math.BigDecimal;

public interface LivePricePort {

    BigDecimal getCurrentPrice(Long exchangeCoinId);
}
