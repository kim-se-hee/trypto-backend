package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.vo.ViolationRecord;

import java.util.List;

public interface ViolationRecordQueryPort {

    List<ViolationRecord> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId);
}
