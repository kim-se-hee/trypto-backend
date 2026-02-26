package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.trading.application.port.out.InvestmentRulePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockInvestmentRuleAdapter implements InvestmentRulePort {

    private final Map<Long, List<InvestmentRuleData>> rulesByRoundId = new ConcurrentHashMap<>();

    @Override
    public List<InvestmentRuleData> findByRoundId(Long roundId) {
        return rulesByRoundId.getOrDefault(roundId, List.of());
    }

    public void addRule(Long roundId, InvestmentRuleData rule) {
        rulesByRoundId.computeIfAbsent(roundId, k -> new ArrayList<>()).add(rule);
    }

    public void clear() {
        rulesByRoundId.clear();
    }
}
