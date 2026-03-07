package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.trading.application.port.out.WalletBalanceQueryPort;
import ksh.tryptobackend.wallet.application.port.in.GetAvailableBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletBalanceQueryAdapter implements WalletBalanceQueryPort {

    private final GetAvailableBalanceUseCase getAvailableBalanceUseCase;

    @Override
    public BigDecimal getAvailableBalance(Long walletId, Long coinId) {
        return getAvailableBalanceUseCase.getAvailableBalance(walletId, coinId);
    }
}
