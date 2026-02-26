package ksh.tryptobackend.trading.application.port.out;

import ksh.tryptobackend.trading.domain.vo.RuleType;

import java.math.BigDecimal;
import java.util.List;

public interface InvestmentRulePort {

    List<InvestmentRuleData> findByRoundId(Long roundId);

    record InvestmentRuleData(
        Long ruleId,
        RuleType ruleType,
        BigDecimal thresholdValue
    ) {
    }
}
