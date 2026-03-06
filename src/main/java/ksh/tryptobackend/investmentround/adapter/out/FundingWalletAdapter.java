package ksh.tryptobackend.investmentround.adapter.out;

import ksh.tryptobackend.investmentround.application.port.out.FundingWalletPort;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import ksh.tryptobackend.wallet.application.port.in.ManageWalletBalanceUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FundingWalletAdapter implements FundingWalletPort {

    private final FindWalletUseCase findWalletUseCase;
    private final ManageWalletBalanceUseCase manageWalletBalanceUseCase;

    @Override
    public Optional<Long> findWalletId(Long roundId, Long exchangeId) {
        return findWalletUseCase.findByRoundIdAndExchangeId(roundId, exchangeId)
            .map(WalletResult::walletId);
    }

    @Override
    public void addBalance(Long walletId, Long coinId, BigDecimal amount) {
        manageWalletBalanceUseCase.addBalance(walletId, coinId, amount);
    }
}
