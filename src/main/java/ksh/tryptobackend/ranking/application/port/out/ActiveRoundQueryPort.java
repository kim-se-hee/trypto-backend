package ksh.tryptobackend.ranking.application.port.out;

import ksh.tryptobackend.ranking.domain.vo.ActiveRound;

import java.util.List;
import java.util.Optional;

public interface ActiveRoundQueryPort {

    Optional<ActiveRound> findActiveRoundByUserId(Long userId);

    List<ActiveRound> findAllActiveRounds();
}
