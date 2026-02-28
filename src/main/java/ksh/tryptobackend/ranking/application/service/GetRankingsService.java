package ksh.tryptobackend.ranking.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.ranking.application.port.in.GetRankingsUseCase;
import ksh.tryptobackend.ranking.application.port.in.dto.query.GetRankingsQuery;
import ksh.tryptobackend.ranking.application.port.in.dto.result.RankingItemResult;
import ksh.tryptobackend.ranking.application.port.out.RankingPersistencePort;
import ksh.tryptobackend.ranking.application.port.out.dto.RankingWithUserProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetRankingsService implements GetRankingsUseCase {

    private final RankingPersistencePort rankingPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public Page<RankingItemResult> getRankings(GetRankingsQuery query) {
        LocalDate referenceDate = resolveReferenceDate(query);
        Page<RankingWithUserProjection> rankings = rankingPersistencePort.findRankings(
            query.period(), referenceDate, query.pageable());
        return rankings.map(RankingItemResult::from);
    }

    private LocalDate resolveReferenceDate(GetRankingsQuery query) {
        if (query.referenceDate() != null) {
            return query.referenceDate();
        }
        return rankingPersistencePort.findLatestReferenceDate(query.period())
            .orElseThrow(() -> new CustomException(ErrorCode.RANKING_NOT_FOUND));
    }
}
