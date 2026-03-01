package ksh.tryptobackend.investmentround.application.service;

import ksh.tryptobackend.investmentround.application.port.in.EndRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.command.EndRoundCommand;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.EndRoundResult;
import org.springframework.stereotype.Service;

@Service
public class EndRoundService implements EndRoundUseCase {

    @Override
    public EndRoundResult endRound(EndRoundCommand command) {
        throw new UnsupportedOperationException("EndRoundUseCase is not implemented yet.");
    }
}
