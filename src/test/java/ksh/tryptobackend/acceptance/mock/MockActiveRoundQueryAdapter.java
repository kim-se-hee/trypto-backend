package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.ranking.application.port.out.ActiveRoundQueryPort;
import ksh.tryptobackend.ranking.domain.vo.ActiveRound;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MockActiveRoundQueryAdapter implements ActiveRoundQueryPort {

    private final List<ActiveRound> activeRounds = new ArrayList<>();

    @Override
    public Optional<ActiveRound> findActiveRoundByUserId(Long userId) {
        return activeRounds.stream()
            .filter(r -> r.userId().equals(userId))
            .findFirst();
    }

    @Override
    public List<ActiveRound> findAllActiveRounds() {
        return List.copyOf(activeRounds);
    }

    public void addActiveRound(Long roundId, Long userId, LocalDateTime startedAt) {
        activeRounds.add(new ActiveRound(roundId, userId, startedAt));
    }

    public void clear() {
        activeRounds.clear();
    }
}
