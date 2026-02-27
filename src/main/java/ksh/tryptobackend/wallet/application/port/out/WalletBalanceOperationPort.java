package ksh.tryptobackend.wallet.application.port.out;

import java.math.BigDecimal;

public interface WalletBalanceOperationPort {

    BigDecimal getAvailableBalance(Long walletId, Long coinId);

    void deductBalance(Long walletId, Long coinId, BigDecimal amount);

    void addBalance(Long walletId, Long coinId, BigDecimal amount);

    void lockBalance(Long walletId, Long coinId, BigDecimal amount);

    void unlockBalance(Long walletId, Long coinId, BigDecimal amount);
}
