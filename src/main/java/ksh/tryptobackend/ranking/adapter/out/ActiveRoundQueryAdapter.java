package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindActiveRoundsUseCase;
import ksh.tryptobackend.investmentround.application.port.in.FindRoundInfoUseCase;
import ksh.tryptobackend.ranking.application.port.out.ActiveRoundQueryPort;
import ksh.tryptobackend.ranking.domain.vo.ActiveRound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ActiveRoundQueryAdapter implements ActiveRoundQueryPort {

    private final FindRoundInfoUseCase findRoundInfoUseCase;
    private final FindActiveRoundsUseCase findActiveRoundsUseCase;

    @Override
    public Optional<ActiveRound> findActiveRoundByUserId(Long userId) {
        return findRoundInfoUseCase.findActiveByUserId(userId)
            .map(result -> new ActiveRound(result.roundId(), result.userId(), result.startedAt()));
    }

    @Override
    public List<ActiveRound> findAllActiveRounds() {
        return findActiveRoundsUseCase.findAllActiveRounds().stream()
            .map(r -> new ActiveRound(r.roundId(), r.userId(), r.startedAt()))
            .toList();
    }
}
