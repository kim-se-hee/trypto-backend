package ksh.tryptobackend.ranking.application.port.out;

import ksh.tryptobackend.ranking.domain.vo.ActiveRound;

import java.util.List;

public interface ActiveRoundQueryPort {

    List<ActiveRound> findAllActiveRounds();
}
