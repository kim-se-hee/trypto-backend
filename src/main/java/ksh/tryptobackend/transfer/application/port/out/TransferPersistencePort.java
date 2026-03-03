package ksh.tryptobackend.transfer.application.port.out;

import ksh.tryptobackend.transfer.domain.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransferPersistencePort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByIdempotencyKey(UUID idempotencyKey);

    Page<Transfer> findByFromWalletId(Long walletId, Pageable pageable);

    Page<Transfer> findByToWalletId(Long walletId, Pageable pageable);

    Page<Transfer> findByWalletId(Long walletId, Pageable pageable);
}
