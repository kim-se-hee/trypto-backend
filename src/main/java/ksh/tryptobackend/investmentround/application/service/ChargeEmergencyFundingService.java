package ksh.tryptobackend.investmentround.application.service;

import ksh.tryptobackend.investmentround.application.port.in.ChargeEmergencyFundingUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.command.ChargeEmergencyFundingCommand;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ChargeEmergencyFundingResult;
import org.springframework.stereotype.Service;

@Service
public class ChargeEmergencyFundingService implements ChargeEmergencyFundingUseCase {

    @Override
    public ChargeEmergencyFundingResult chargeEmergencyFunding(ChargeEmergencyFundingCommand command) {
        throw new UnsupportedOperationException("ChargeEmergencyFundingUseCase is not implemented yet.");
    }
}
