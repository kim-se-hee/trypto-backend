package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressInfo;

import java.util.Optional;

public interface DepositAddressQueryPort {

    Optional<DepositAddressInfo> findByRoundIdAndChainAndAddress(Long roundId, String chain, String address);
}
