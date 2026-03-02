package ksh.tryptobackend.regretanalysis.domain.model;

import ksh.tryptobackend.regretanalysis.domain.vo.ImpactGap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ViolationDetails {

    private final List<ViolationDetail> details;

    public ViolationDetails(List<ViolationDetail> details) {
        this.details = List.copyOf(details);
    }

    public List<RuleImpact> toRuleImpacts(BigDecimal totalAsset,
                                           BigDecimal totalInvestment,
                                           BigDecimal actualProfitRate) {
        Map<Long, List<ViolationDetail>> detailsByRule = details.stream()
            .collect(Collectors.groupingBy(ViolationDetail::getRuleId));

        List<RuleImpact> impacts = new ArrayList<>();
        for (Map.Entry<Long, List<ViolationDetail>> entry : detailsByRule.entrySet()) {
            Long ruleId = entry.getKey();
            List<ViolationDetail> ruleDetails = entry.getValue();

            BigDecimal totalLossAmount = ruleDetails.stream()
                .map(ViolationDetail::getLossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ruleFollowedAsset = totalAsset.add(totalLossAmount);
            BigDecimal ruleFollowedRate = RegretReport.calculateProfitRate(
                ruleFollowedAsset, totalInvestment);
            BigDecimal gap = ruleFollowedRate.subtract(actualProfitRate);

            impacts.add(RuleImpact.create(ruleId, ruleDetails.size(),
                totalLossAmount, ImpactGap.of(gap)));
        }

        return impacts;
    }

    public Set<Long> extractCoinIds() {
        return details.stream()
            .map(ViolationDetail::getCoinId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public List<ViolationDetail> toList() {
        return details;
    }

    public int size() {
        return details.size();
    }

    public boolean isEmpty() {
        return details.isEmpty();
    }
}
