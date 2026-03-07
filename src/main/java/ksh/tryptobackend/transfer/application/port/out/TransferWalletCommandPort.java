package ksh.tryptobackend.transfer.application.port.out;

import java.math.BigDecimal;

public interface TransferWalletCommandPort {

    void deductBalance(Long walletId, Long coinId, BigDecimal amount);

    void addBalance(Long walletId, Long coinId, BigDecimal amount);

    void lockBalance(Long walletId, Long coinId, BigDecimal amount);
}
