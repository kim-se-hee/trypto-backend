package ksh.tryptobackend.investmentround.application.port.out;

import ksh.tryptobackend.investmentround.domain.model.EmergencyFunding;

import java.util.Optional;
import java.util.UUID;

public interface EmergencyFundingPersistencePort {

    Optional<EmergencyFunding> findByRoundIdAndIdempotencyKey(Long roundId, UUID idempotencyKey);

    EmergencyFunding save(EmergencyFunding funding);
}
