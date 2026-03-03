package ksh.tryptobackend.ranking.domain.vo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class SnapshotSummaries {

    private final Map<RoundKey, BigDecimal> totalAssetMap;

    public SnapshotSummaries(Map<RoundKey, BigDecimal> totalAssetMap) {
        this.totalAssetMap = totalAssetMap;
    }

    public Optional<ProfitRate> calculateProfitRate(RoundKey key, SnapshotSummaries base) {
        BigDecimal current = totalAssetMap.get(key);
        BigDecimal baseline = base.totalAssetMap.get(key);

        if (current == null || baseline == null) {
            return Optional.empty();
        }

        return Optional.of(ProfitRate.fromAssetChange(current, baseline));
    }
}
