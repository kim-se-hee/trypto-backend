package ksh.tryptobackend.investmentround.application.port.out;

import java.util.Optional;

public interface FundingWalletQueryPort {

    Optional<Long> findWalletId(Long roundId, Long exchangeId);
}
