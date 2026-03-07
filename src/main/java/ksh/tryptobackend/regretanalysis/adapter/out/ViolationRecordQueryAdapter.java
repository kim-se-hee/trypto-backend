package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.trading.application.port.in.FindViolationsUseCase;
import ksh.tryptobackend.regretanalysis.application.port.out.ViolationRecordQueryPort;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ViolationRecordQueryAdapter implements ViolationRecordQueryPort {

    private final FindViolationsUseCase findViolationsUseCase;

    @Override
    public List<ViolationRecord> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId) {
        return findViolationsUseCase.findByRuleIdsAndExchangeId(ruleIds, exchangeId).stream()
            .map(result -> new ViolationRecord(result.violationId(), result.orderId(), result.ruleId(), result.createdAt()))
            .toList();
    }
}
