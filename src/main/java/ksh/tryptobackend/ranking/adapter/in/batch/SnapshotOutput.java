package ksh.tryptobackend.ranking.adapter.in.batch;

import ksh.tryptobackend.ranking.domain.model.PortfolioSnapshot;
import ksh.tryptobackend.ranking.domain.model.SnapshotDetail;

import java.util.List;

public record SnapshotOutput(PortfolioSnapshot snapshot, List<SnapshotDetail> details) {
}
