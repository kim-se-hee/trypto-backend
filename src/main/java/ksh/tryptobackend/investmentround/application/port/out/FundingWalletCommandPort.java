package ksh.tryptobackend.investmentround.application.port.out;

import java.math.BigDecimal;

public interface FundingWalletCommandPort {

    void addBalance(Long walletId, Long coinId, BigDecimal amount);
}
