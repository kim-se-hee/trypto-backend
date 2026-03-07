package ksh.tryptobackend.trading.application.port.out;

import java.math.BigDecimal;

public interface WalletBalanceQueryPort {

    BigDecimal getAvailableBalance(Long walletId, Long coinId);
}
