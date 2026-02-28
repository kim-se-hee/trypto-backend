package ksh.tryptobackend.ranking.application.port.in.dto.result;

import ksh.tryptobackend.ranking.application.port.out.dto.RankingStatsProjection;

import java.math.BigDecimal;

public record RankingStatsResult(
    long totalParticipants,
    BigDecimal maxProfitRate,
    BigDecimal avgProfitRate
) {
    public static RankingStatsResult from(RankingStatsProjection projection) {
        return new RankingStatsResult(
            projection.totalParticipants(),
            projection.maxProfitRate(),
            projection.avgProfitRate()
        );
    }
}
