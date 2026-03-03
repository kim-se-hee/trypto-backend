package ksh.tryptobackend.ranking.application.port.out;

import ksh.tryptobackend.ranking.domain.model.PortfolioSnapshot;
import ksh.tryptobackend.ranking.domain.model.SnapshotDetail;

import java.util.List;

public interface SnapshotPersistencePort {

    PortfolioSnapshot save(PortfolioSnapshot snapshot);

    void saveDetails(Long snapshotId, List<SnapshotDetail> details);
}
