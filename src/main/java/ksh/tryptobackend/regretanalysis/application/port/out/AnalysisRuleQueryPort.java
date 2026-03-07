package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisRules;

public interface AnalysisRuleQueryPort {

    AnalysisRules findByRoundId(Long roundId);
}
