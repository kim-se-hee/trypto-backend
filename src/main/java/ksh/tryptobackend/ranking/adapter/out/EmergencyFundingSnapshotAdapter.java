package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.SumEmergencyFundingUseCase;
import ksh.tryptobackend.ranking.application.port.out.EmergencyFundingSnapshotPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("rankingEmergencyFundingSnapshotAdapter")
@RequiredArgsConstructor
public class EmergencyFundingSnapshotAdapter implements EmergencyFundingSnapshotPort {

    private final SumEmergencyFundingUseCase sumEmergencyFundingUseCase;

    @Override
    public BigDecimal sumByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        return sumEmergencyFundingUseCase.sumByRoundIdAndExchangeId(roundId, exchangeId);
    }
}
