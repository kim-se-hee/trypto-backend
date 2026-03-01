package ksh.tryptobackend.investmentround.application.service;

import ksh.tryptobackend.investmentround.application.port.in.GetActiveRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.query.GetActiveRoundQuery;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.GetActiveRoundResult;
import org.springframework.stereotype.Service;

@Service
public class GetActiveRoundService implements GetActiveRoundUseCase {

    @Override
    public GetActiveRoundResult getActiveRound(GetActiveRoundQuery query) {
        throw new UnsupportedOperationException("GetActiveRoundUseCase is not implemented yet.");
    }
}
