package ksh.tryptobackend.investmentround.adapter.in.dto.response;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.GetActiveRoundResult;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.GetActiveRoundRuleResult;
import ksh.tryptobackend.investmentround.domain.vo.RoundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetActiveRoundResponse(
    Long roundId,
    Long userId,
    long roundNumber,
    RoundStatus status,
    BigDecimal initialSeed,
    BigDecimal emergencyFundingLimit,
    int emergencyChargeCount,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    List<RuleResponse> rules
) {

    public static GetActiveRoundResponse from(GetActiveRoundResult result) {
        return new GetActiveRoundResponse(
            result.roundId(),
            result.userId(),
            result.roundNumber(),
            result.status(),
            result.initialSeed(),
            result.emergencyFundingLimit(),
            result.emergencyChargeCount(),
            result.startedAt(),
            result.endedAt(),
            result.rules().stream().map(RuleResponse::from).toList()
        );
    }

    public record RuleResponse(Long ruleId, RuleType ruleType, BigDecimal thresholdValue) {

        public static RuleResponse from(GetActiveRoundRuleResult result) {
            return new RuleResponse(result.ruleId(), result.ruleType(), result.thresholdValue());
        }
    }
}
