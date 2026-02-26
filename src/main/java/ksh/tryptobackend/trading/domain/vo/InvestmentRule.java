package ksh.tryptobackend.trading.domain.vo;

import java.math.BigDecimal;

public record InvestmentRule(
    Long ruleId,
    RuleType ruleType,
    BigDecimal thresholdValue
) {
}
