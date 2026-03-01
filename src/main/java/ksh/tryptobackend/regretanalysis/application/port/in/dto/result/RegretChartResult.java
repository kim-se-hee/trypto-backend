package ksh.tryptobackend.regretanalysis.application.port.in.dto.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RegretChartResult(
    Long roundId,
    Long exchangeId,
    String exchangeName,
    String currency,
    int totalDays,
    List<DailyAssetResult> assetHistory,
    List<ViolationMarkerResult> violationMarkers
) {

    public record DailyAssetResult(
        LocalDate snapshotDate,
        BigDecimal actualAsset,
        BigDecimal ruleFollowedAsset,
        BigDecimal btcHoldAsset
    ) {
    }

    public record ViolationMarkerResult(
        LocalDate snapshotDate,
        BigDecimal assetValue
    ) {
    }
}
