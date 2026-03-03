package ksh.tryptobackend.ranking.adapter.out.repository;

import ksh.tryptobackend.ranking.adapter.out.entity.RankingJpaEntity;
import ksh.tryptobackend.ranking.domain.vo.RankingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface RankingJpaRepository extends JpaRepository<RankingJpaEntity, Long> {

    void deleteByPeriodAndReferenceDate(RankingPeriod period, LocalDate referenceDate);
}
