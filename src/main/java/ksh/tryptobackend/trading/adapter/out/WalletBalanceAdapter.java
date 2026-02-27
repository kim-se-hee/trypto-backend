package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.trading.application.port.out.WalletBalancePort;
import ksh.tryptobackend.wallet.application.port.out.WalletBalanceOperationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletBalanceAdapter implements WalletBalancePort {

    private final WalletBalanceOperationPort walletBalanceOperationPort;

    @Override
    public BigDecimal getAvailableBalance(Long walletId, Long coinId) {
        return walletBalanceOperationPort.getAvailableBalance(walletId, coinId);
    }

    @Override
    public void deductBalance(Long walletId, Long coinId, BigDecimal amount) {
        walletBalanceOperationPort.deductBalance(walletId, coinId, amount);
    }

    @Override
    public void addBalance(Long walletId, Long coinId, BigDecimal amount) {
        walletBalanceOperationPort.addBalance(walletId, coinId, amount);
    }

    @Override
    public void lockBalance(Long walletId, Long coinId, BigDecimal amount) {
        walletBalanceOperationPort.lockBalance(walletId, coinId, amount);
    }

    @Override
    public void unlockBalance(Long walletId, Long coinId, BigDecimal amount) {
        walletBalanceOperationPort.unlockBalance(walletId, coinId, amount);
    }
}
