package ksh.tryptobackend.regretanalysis.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AssetSnapshot {

    private final Long snapshotId;
    private final Long roundId;
    private final Long exchangeId;
    private final BigDecimal totalAsset;
    private final BigDecimal totalInvestment;
    private final BigDecimal totalProfitRate;
    private final LocalDateTime snapshotDate;

    public static AssetSnapshot reconstitute(Long snapshotId, Long roundId, Long exchangeId,
                                              BigDecimal totalAsset, BigDecimal totalInvestment,
                                              BigDecimal totalProfitRate, LocalDateTime snapshotDate) {
        return AssetSnapshot.builder()
            .snapshotId(snapshotId)
            .roundId(roundId)
            .exchangeId(exchangeId)
            .totalAsset(totalAsset)
            .totalInvestment(totalInvestment)
            .totalProfitRate(totalProfitRate)
            .snapshotDate(snapshotDate)
            .build();
    }

    public LocalDate getSnapshotLocalDate() {
        return snapshotDate.toLocalDate();
    }
}
