package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.command.EndRoundCommand;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.EndRoundResult;

public interface EndRoundUseCase {

    EndRoundResult endRound(EndRoundCommand command);
}
