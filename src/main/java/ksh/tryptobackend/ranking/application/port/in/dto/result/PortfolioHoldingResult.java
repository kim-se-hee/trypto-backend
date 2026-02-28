package ksh.tryptobackend.ranking.application.port.in.dto.result;

import ksh.tryptobackend.ranking.application.port.out.dto.SnapshotDetailProjection;

import java.math.BigDecimal;

public record PortfolioHoldingResult(
    String coinSymbol,
    String exchangeName,
    BigDecimal assetRatio,
    BigDecimal profitRate
) {

    public static PortfolioHoldingResult from(SnapshotDetailProjection projection) {
        return new PortfolioHoldingResult(
            projection.coinSymbol(),
            projection.exchangeName(),
            projection.assetRatio(),
            projection.profitRate()
        );
    }
}
