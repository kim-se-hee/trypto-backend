package ksh.tryptobackend.ranking.application.port.out;

import ksh.tryptobackend.ranking.domain.model.Ranking;
import ksh.tryptobackend.ranking.domain.vo.RankingPeriod;

import java.time.LocalDate;
import java.util.List;

public interface RankingWritePort {

    void deleteByPeriodAndDate(RankingPeriod period, LocalDate referenceDate);

    void saveAll(List<Ranking> rankings);
}
