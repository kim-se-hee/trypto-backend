package ksh.tryptobackend.ranking.application.port.in;

import ksh.tryptobackend.ranking.application.port.in.dto.query.GetRankingsQuery;
import ksh.tryptobackend.ranking.application.port.in.dto.result.RankingItemResult;
import org.springframework.data.domain.Page;

public interface GetRankingsUseCase {

    Page<RankingItemResult> getRankings(GetRankingsQuery query);
}
