package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.ranking.application.port.out.SnapshotQueryPort;
import ksh.tryptobackend.ranking.application.port.out.dto.SnapshotInfo;
import ksh.tryptobackend.regretanalysis.application.port.out.PortfolioSnapshotPort;
import ksh.tryptobackend.regretanalysis.domain.model.AssetSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PortfolioSnapshotAdapter implements PortfolioSnapshotPort {

    private final SnapshotQueryPort snapshotQueryPort;

    @Override
    public AssetSnapshot getLatestByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        return snapshotQueryPort.findLatestByRoundIdAndExchangeId(roundId, exchangeId)
            .map(this::toAssetSnapshot)
            .orElseThrow(() -> new IllegalStateException(
                "스냅샷이 존재해야 하지만 찾을 수 없습니다: roundId=" + roundId + ", exchangeId=" + exchangeId));
    }

    @Override
    public List<AssetSnapshot> findAllByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        return snapshotQueryPort.findAllByRoundIdAndExchangeId(roundId, exchangeId).stream()
            .map(this::toAssetSnapshot)
            .toList();
    }

    private AssetSnapshot toAssetSnapshot(SnapshotInfo info) {
        return AssetSnapshot.reconstitute(
            info.snapshotId(), info.roundId(), info.exchangeId(),
            info.totalAsset(), info.totalInvestment(),
            info.totalProfitRate(), info.snapshotDate()
        );
    }
}
