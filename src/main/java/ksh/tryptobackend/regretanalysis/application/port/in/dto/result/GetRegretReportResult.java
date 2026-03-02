package ksh.tryptobackend.regretanalysis.application.port.in.dto.result;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.ExchangeMetadata;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RuleInfo;
import ksh.tryptobackend.regretanalysis.domain.model.RegretReport;
import ksh.tryptobackend.regretanalysis.domain.model.RuleImpact;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;
import ksh.tryptobackend.regretanalysis.domain.vo.ThresholdUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public record GetRegretReportResult(
    Long reportId,
    Long roundId,
    Long exchangeId,
    String exchangeName,
    String currency,
    int totalViolations,
    LocalDate analysisStart,
    LocalDate analysisEnd,
    BigDecimal missedProfit,
    BigDecimal actualProfitRate,
    BigDecimal ruleFollowedProfitRate,
    List<RuleImpactResult> ruleImpacts,
    List<ViolationDetailResult> violationDetails
) {

    public static GetRegretReportResult from(RegretReport report,
                                             ExchangeMetadata exchange,
                                             Map<Long, RuleInfo> ruleMap,
                                             Map<Long, String> coinSymbols) {
        List<RuleImpactResult> ruleImpactResults = report.getRuleImpacts().stream()
            .map(ri -> toRuleImpactResult(ri, ruleMap))
            .toList();

        List<ViolationDetailResult> violationDetailResults = groupViolationDetails(
            report.getViolationDetails().toList(), ruleMap, coinSymbols);

        return new GetRegretReportResult(
            report.getReportId(),
            report.getRoundId(),
            report.getExchangeId(),
            exchange.name(),
            exchange.currency(),
            report.getTotalViolations(),
            report.getAnalysisStart(),
            report.getAnalysisEnd(),
            report.getMissedProfit(),
            report.getActualProfitRate(),
            report.getRuleFollowedProfitRate(),
            ruleImpactResults,
            violationDetailResults
        );
    }

    private static RuleImpactResult toRuleImpactResult(RuleImpact ruleImpact,
                                                        Map<Long, RuleInfo> ruleMap) {
        RuleInfo rule = ruleMap.get(ruleImpact.getRuleId());
        RuleType ruleType = rule != null ? rule.ruleType() : null;
        BigDecimal thresholdValue = rule != null ? rule.thresholdValue() : BigDecimal.ZERO;
        String thresholdUnit = ruleType != null ? ThresholdUnit.from(ruleType).symbol() : "";

        return new RuleImpactResult(
            ruleImpact.getRuleImpactId(),
            ruleImpact.getRuleId(),
            ruleType,
            thresholdValue,
            thresholdUnit,
            ruleImpact.getViolationCount(),
            ruleImpact.getTotalLossAmount(),
            ruleImpact.getImpactGap().value()
        );
    }

    private static List<ViolationDetailResult> groupViolationDetails(
            List<ViolationDetail> details,
            Map<Long, RuleInfo> ruleMap,
            Map<Long, String> coinSymbols) {
        Map<Long, List<ViolationDetail>> orderGroup = new LinkedHashMap<>();
        List<ViolationDetail> monitoringViolations = new ArrayList<>();

        for (ViolationDetail detail : details) {
            if (detail.getOrderId() != null) {
                orderGroup.computeIfAbsent(detail.getOrderId(), k -> new ArrayList<>()).add(detail);
            } else {
                monitoringViolations.add(detail);
            }
        }

        List<ViolationDetailResult> results = new ArrayList<>();

        for (Map.Entry<Long, List<ViolationDetail>> entry : orderGroup.entrySet()) {
            List<ViolationDetail> grouped = entry.getValue();
            ViolationDetail first = grouped.getFirst();

            List<String> violatedRules = grouped.stream()
                .map(d -> ruleMap.get(d.getRuleId()))
                .filter(Objects::nonNull)
                .map(r -> r.ruleType().name())
                .distinct()
                .toList();

            String coinSymbol = coinSymbols.getOrDefault(first.getCoinId(), "");

            results.add(new ViolationDetailResult(
                first.getViolationDetailId(), first.getOrderId(), coinSymbol,
                violatedRules, first.getProfitLoss(), first.getOccurredAt()));
        }

        for (ViolationDetail detail : monitoringViolations) {
            RuleInfo rule = ruleMap.get(detail.getRuleId());
            String ruleName = rule != null ? rule.ruleType().name() : "";
            String coinSymbol = coinSymbols.getOrDefault(detail.getCoinId(), "");

            results.add(new ViolationDetailResult(
                detail.getViolationDetailId(), null, coinSymbol,
                List.of(ruleName), detail.getProfitLoss(), detail.getOccurredAt()));
        }

        return results;
    }

    public record RuleImpactResult(
        Long ruleImpactId,
        Long ruleId,
        RuleType ruleType,
        BigDecimal thresholdValue,
        String thresholdUnit,
        int violationCount,
        BigDecimal totalLossAmount,
        BigDecimal impactGap
    ) {
    }

    public record ViolationDetailResult(
        Long violationDetailId,
        Long orderId,
        String coinSymbol,
        List<String> violatedRules,
        BigDecimal profitLoss,
        LocalDateTime occurredAt
    ) {
    }
}
