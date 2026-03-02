package ksh.tryptobackend.investmentround.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.investmentround.application.port.in.EndRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.command.EndRoundCommand;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.EndRoundResult;
import ksh.tryptobackend.investmentround.application.port.out.InvestmentRoundPersistencePort;
import ksh.tryptobackend.investmentround.domain.model.InvestmentRound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EndRoundService implements EndRoundUseCase {

    private final InvestmentRoundPersistencePort investmentRoundPersistencePort;
    private final Clock clock;

    @Override
    @Transactional
    public EndRoundResult endRound(EndRoundCommand command) {
        InvestmentRound round = getRound(command.roundId());
        round.validateOwnedBy(command.userId());

        if (round.isEnded()) {
            return toResult(round);
        }

        InvestmentRound endedRound = round.end(LocalDateTime.now(clock));
        InvestmentRound savedRound = investmentRoundPersistencePort.save(endedRound);

        return toResult(savedRound);
    }

    private InvestmentRound getRound(Long roundId) {
        return investmentRoundPersistencePort.findById(roundId)
            .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
    }

    private EndRoundResult toResult(InvestmentRound round) {
        return new EndRoundResult(round.getRoundId(), round.getStatus(), round.getEndedAt());
    }
}
