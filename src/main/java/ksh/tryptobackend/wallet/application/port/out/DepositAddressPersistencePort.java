package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.domain.model.DepositAddress;

import java.util.Optional;

public interface DepositAddressPersistencePort {

    Optional<DepositAddress> findByWalletIdAndChain(Long walletId, String chain);

    DepositAddress save(DepositAddress depositAddress);
}
