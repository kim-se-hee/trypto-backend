package ksh.tryptobackend.ranking.adapter.in.batch;

import ksh.tryptobackend.ranking.application.port.out.ActiveRoundQueryPort;
import ksh.tryptobackend.ranking.application.port.out.RankingEligibilityPort;
import ksh.tryptobackend.ranking.application.port.out.RankingWritePort;
import ksh.tryptobackend.ranking.application.port.out.SnapshotAggregationPort;
import ksh.tryptobackend.ranking.application.port.out.TradeCountPort;
import ksh.tryptobackend.ranking.application.port.out.WalletSnapshotPort;
import ksh.tryptobackend.ranking.application.port.out.dto.ActiveRoundInfo;
import ksh.tryptobackend.ranking.application.port.out.dto.UserSnapshotSummary;
import ksh.tryptobackend.ranking.application.port.out.dto.WalletSnapshotInfo;
import ksh.tryptobackend.ranking.domain.model.Ranking;
import ksh.tryptobackend.ranking.domain.vo.ProfitRate;
import ksh.tryptobackend.ranking.domain.vo.RankingCandidate;
import ksh.tryptobackend.ranking.domain.vo.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
public class RankingTasklet implements Tasklet {

    private static final int RATE_SCALE = 4;
    private static final int ELIGIBILITY_HOURS = 24;

    private final ActiveRoundQueryPort activeRoundQueryPort;
    private final WalletSnapshotPort walletSnapshotPort;
    private final RankingEligibilityPort rankingEligibilityPort;
    private final TradeCountPort tradeCountPort;
    private final SnapshotAggregationPort snapshotAggregationPort;
    private final RankingWritePort rankingWritePort;

    @Value("#{jobParameters['snapshotDate']}")
    private String snapshotDateParam;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate snapshotDate = LocalDate.parse(snapshotDateParam);
        LocalDateTime eligibilityCutoff = snapshotDate.atStartOfDay().minusHours(ELIGIBILITY_HOURS);

        List<ActiveRoundInfo> eligibleRounds = filterEligibleRounds(eligibilityCutoff);
        if (eligibleRounds.isEmpty()) {
            return RepeatStatus.FINISHED;
        }

        Map<RoundKey, UserSnapshotSummary> summaryMap = buildSummaryMap(snapshotDate);
        List<RankingCandidate> candidates = buildCandidates(eligibleRounds, summaryMap);
        Collections.sort(candidates);

        List<Ranking> rankings = assignRanks(candidates, snapshotDate);
        saveRankings(rankings, snapshotDate);

        return RepeatStatus.FINISHED;
    }

    private List<ActiveRoundInfo> filterEligibleRounds(LocalDateTime eligibilityCutoff) {
        return activeRoundQueryPort.findAllActiveRounds().stream()
            .filter(round -> round.startedAt().isBefore(eligibilityCutoff))
            .filter(this::hasAnyFilledOrder)
            .toList();
    }

    private boolean hasAnyFilledOrder(ActiveRoundInfo round) {
        return walletSnapshotPort.findByRoundId(round.roundId()).stream()
            .anyMatch(wallet -> rankingEligibilityPort.hasFilledOrders(wallet.walletId()));
    }

    private Map<RoundKey, UserSnapshotSummary> buildSummaryMap(LocalDate snapshotDate) {
        return snapshotAggregationPort.findLatestSummaries(snapshotDate).stream()
            .collect(Collectors.toMap(
                s -> new RoundKey(s.userId(), s.roundId()),
                s -> s
            ));
    }

    private List<RankingCandidate> buildCandidates(List<ActiveRoundInfo> eligibleRounds,
                                                   Map<RoundKey, UserSnapshotSummary> summaryMap) {
        List<RankingCandidate> candidates = new ArrayList<>();
        for (ActiveRoundInfo round : eligibleRounds) {
            UserSnapshotSummary summary = summaryMap.get(new RoundKey(round.userId(), round.roundId()));
            if (summary == null) {
                continue;
            }

            BigDecimal profitRate = calculateProfitRate(summary.totalAssetKrw(), summary.totalInvestmentKrw());
            int tradeCount = countTradesForRound(round.roundId());

            candidates.add(new RankingCandidate(
                round.userId(), round.roundId(), profitRate, tradeCount, round.startedAt()
            ));
        }
        return candidates;
    }

    private BigDecimal calculateProfitRate(BigDecimal totalAssetKrw, BigDecimal totalInvestmentKrw) {
        if (totalInvestmentKrw.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalAssetKrw.subtract(totalInvestmentKrw)
            .divide(totalInvestmentKrw, RATE_SCALE, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    private int countTradesForRound(Long roundId) {
        return walletSnapshotPort.findByRoundId(roundId).stream()
            .mapToInt(wallet -> tradeCountPort.countFilledOrders(wallet.walletId()))
            .sum();
    }

    private List<Ranking> assignRanks(List<RankingCandidate> candidates, LocalDate referenceDate) {
        List<Ranking> rankings = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            RankingCandidate candidate = candidates.get(i);
            rankings.add(Ranking.create(
                candidate.userId(), candidate.roundId(), RankingPeriod.DAILY,
                i + 1, ProfitRate.of(candidate.profitRate()), candidate.tradeCount(),
                referenceDate
            ));
        }
        return rankings;
    }

    private void saveRankings(List<Ranking> dailyRankings, LocalDate snapshotDate) {
        saveForPeriod(dailyRankings, RankingPeriod.DAILY, snapshotDate);

        if (snapshotDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            saveForPeriod(dailyRankings, RankingPeriod.WEEKLY, snapshotDate);
        }
        if (snapshotDate.getDayOfMonth() == 1) {
            saveForPeriod(dailyRankings, RankingPeriod.MONTHLY, snapshotDate);
        }
    }

    private void saveForPeriod(List<Ranking> dailyRankings, RankingPeriod period, LocalDate referenceDate) {
        rankingWritePort.deleteByPeriodAndDate(period, referenceDate);

        if (period == RankingPeriod.DAILY) {
            rankingWritePort.saveAll(dailyRankings);
        } else {
            List<Ranking> periodRankings = dailyRankings.stream()
                .map(r -> Ranking.create(
                    r.getUserId(), r.getRoundId(), period,
                    r.getRank(), r.getProfitRate(), r.getTradeCount(), referenceDate
                ))
                .toList();
            rankingWritePort.saveAll(periodRankings);
        }
    }

    private record RoundKey(Long userId, Long roundId) {}
}
