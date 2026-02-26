package ksh.tryptobackend.trading.domain.model;

import ksh.tryptobackend.trading.domain.vo.InvestmentRule;
import ksh.tryptobackend.trading.domain.vo.Side;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ViolationChecker {

    public static List<RuleViolation> check(Order order, List<InvestmentRule> rules,
                                            Holding holding, BigDecimal changeRate,
                                            BigDecimal currentPrice, long todayOrderCount,
                                            LocalDateTime now) {
        return rules.stream()
            .map(rule -> checkRule(order, rule, holding, changeRate, currentPrice, todayOrderCount, now))
            .flatMap(Optional::stream)
            .toList();
    }

    private static Optional<RuleViolation> checkRule(Order order, InvestmentRule rule,
                                                     Holding holding, BigDecimal changeRate,
                                                     BigDecimal currentPrice, long todayOrderCount,
                                                     LocalDateTime now) {
        return switch (rule.ruleType()) {
            case CHASE_BUY_BAN -> checkChaseBuyBan(order, rule, changeRate, now);
            case AVERAGING_LIMIT -> checkAveragingLimit(order, rule, holding, currentPrice, now);
            case OVERTRADING_LIMIT -> checkOvertradingLimit(rule, todayOrderCount, now);
        };
    }

    private static Optional<RuleViolation> checkChaseBuyBan(Order order, InvestmentRule rule,
                                                            BigDecimal changeRate, LocalDateTime now) {
        if (order.getSide() != Side.BUY) {
            return Optional.empty();
        }
        if (changeRate.compareTo(rule.thresholdValue()) < 0) {
            return Optional.empty();
        }
        String reason = String.format("상승률 %s%% ≥ %s%%", changeRate, rule.thresholdValue());
        return Optional.of(new RuleViolation(rule.ruleId(), reason, now));
    }

    private static Optional<RuleViolation> checkAveragingLimit(Order order, InvestmentRule rule,
                                                               Holding holding,
                                                               BigDecimal currentPrice, LocalDateTime now) {
        if (order.getSide() != Side.BUY) {
            return Optional.empty();
        }
        if (holding == null) {
            return Optional.empty();
        }
        if (!holding.isAtLoss(currentPrice)) {
            return Optional.empty();
        }
        int newCount = holding.getAveragingDownCount() + 1;
        if (newCount < rule.thresholdValue().intValue()) {
            return Optional.empty();
        }
        String reason = String.format("물타기 %d회 ≥ %d회", newCount, rule.thresholdValue().intValue());
        return Optional.of(new RuleViolation(rule.ruleId(), reason, now));
    }

    private static Optional<RuleViolation> checkOvertradingLimit(InvestmentRule rule,
                                                                 long todayOrderCount, LocalDateTime now) {
        long newCount = todayOrderCount + 1;
        if (newCount < rule.thresholdValue().longValue()) {
            return Optional.empty();
        }
        String reason = String.format("오늘 주문 %d건 ≥ %d건", newCount, rule.thresholdValue().intValue());
        return Optional.of(new RuleViolation(rule.ruleId(), reason, now));
    }
}
