package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindRoundInfoUseCase;
import ksh.tryptobackend.ranking.application.port.out.InvestmentRoundPort;
import ksh.tryptobackend.ranking.application.port.out.dto.RoundInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvestmentRoundAdapter implements InvestmentRoundPort {

    private final FindRoundInfoUseCase findRoundInfoUseCase;

    @Override
    public Optional<RoundInfo> findActiveRoundByUserId(Long userId) {
        return findRoundInfoUseCase.findActiveByUserId(userId)
            .map(result -> new RoundInfo(result.roundId(), result.userId()));
    }
}
