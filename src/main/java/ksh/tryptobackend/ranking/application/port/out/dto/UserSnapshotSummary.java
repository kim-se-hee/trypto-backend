package ksh.tryptobackend.ranking.application.port.out.dto;

import java.math.BigDecimal;

public record UserSnapshotSummary(
    Long userId,
    Long roundId,
    BigDecimal totalAssetKrw,
    BigDecimal totalInvestmentKrw
) {
}
