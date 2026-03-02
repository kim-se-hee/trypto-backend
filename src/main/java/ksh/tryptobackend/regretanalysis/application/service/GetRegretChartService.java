package ksh.tryptobackend.regretanalysis.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.regretanalysis.application.port.in.GetRegretChartUseCase;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.query.GetRegretChartQuery;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult.ChartDataPoint;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult.ViolationMarkerPoint;
import ksh.tryptobackend.regretanalysis.application.port.out.BtcPriceHistoryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.ExchangeInfoPort;
import ksh.tryptobackend.regretanalysis.application.port.out.InvestmentRoundPort;
import ksh.tryptobackend.regretanalysis.application.port.out.PortfolioSnapshotPort;
import ksh.tryptobackend.regretanalysis.application.port.out.RegretReportPersistencePort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.AssetSnapshot;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.BtcDailyPrice;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.ExchangeInfoRecord;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RoundInfoResult;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;
import ksh.tryptobackend.regretanalysis.domain.vo.BtcBenchmark;
import ksh.tryptobackend.regretanalysis.domain.vo.CumulativeLossTimeline;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationMarkers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRegretChartService implements GetRegretChartUseCase {

    private final InvestmentRoundPort investmentRoundPort;
    private final RegretReportPersistencePort regretReportPersistencePort;
    private final PortfolioSnapshotPort portfolioSnapshotPort;
    private final BtcPriceHistoryPort btcPriceHistoryPort;
    private final ExchangeInfoPort exchangeInfoPort;

    @Override
    @Transactional(readOnly = true)
    public RegretChartResult getRegretChart(GetRegretChartQuery query) {
        RoundInfoResult round = getRoundAndValidateOwner(query);
        validateReportExists(query);
        List<ViolationDetail> violations = getViolationDetails(query);
        ExchangeInfoRecord exchangeInfo = getExchangeInfo(query.exchangeId());
        List<AssetSnapshot> snapshots = getSnapshots(query);

        List<LocalDate> snapshotDates = extractSnapshotDates(snapshots);
        Map<LocalDate, BigDecimal> assetByDate = buildAssetByDateMap(snapshots);

        CumulativeLossTimeline lossTimeline = CumulativeLossTimeline.build(violations, snapshotDates);
        BtcBenchmark btcBenchmark = buildBtcBenchmark(snapshots, snapshotDates, exchangeInfo.currency());
        ViolationMarkers violationMarkers = ViolationMarkers.from(violations, assetByDate);

        List<ChartDataPoint> assetHistory = mapToAssetHistory(snapshots, lossTimeline, btcBenchmark);
        List<ViolationMarkerPoint> markerPoints = mapToViolationMarkerPoints(violationMarkers);
        int totalDays = calculateTotalDays(snapshots);

        return new RegretChartResult(
            query.roundId(), query.exchangeId(),
            exchangeInfo.name(), exchangeInfo.currency(),
            totalDays, assetHistory, markerPoints
        );
    }

    private RoundInfoResult getRoundAndValidateOwner(GetRegretChartQuery query) {
        RoundInfoResult round = investmentRoundPort.getRound(query.roundId());
        if (!round.userId().equals(query.userId())) {
            throw new CustomException(ErrorCode.ROUND_ACCESS_DENIED);
        }
        return round;
    }

    private void validateReportExists(GetRegretChartQuery query) {
        if (!regretReportPersistencePort.existsByRoundIdAndExchangeId(query.roundId(), query.exchangeId())) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
    }

    private List<ViolationDetail> getViolationDetails(GetRegretChartQuery query) {
        return regretReportPersistencePort.findViolationDetailsByRoundIdAndExchangeId(
            query.roundId(), query.exchangeId());
    }

    private ExchangeInfoRecord getExchangeInfo(Long exchangeId) {
        return exchangeInfoPort.getExchangeInfo(exchangeId);
    }

    private List<AssetSnapshot> getSnapshots(GetRegretChartQuery query) {
        List<AssetSnapshot> snapshots = portfolioSnapshotPort.findAllByRoundIdAndExchangeId(
            query.roundId(), query.exchangeId());
        if (snapshots.isEmpty()) {
            throw new CustomException(ErrorCode.SNAPSHOT_NOT_FOUND);
        }
        return snapshots;
    }

    private List<LocalDate> extractSnapshotDates(List<AssetSnapshot> snapshots) {
        return snapshots.stream()
            .map(s -> s.snapshotDate().toLocalDate())
            .toList();
    }

    private Map<LocalDate, BigDecimal> buildAssetByDateMap(List<AssetSnapshot> snapshots) {
        return snapshots.stream()
            .collect(Collectors.toMap(
                s -> s.snapshotDate().toLocalDate(),
                AssetSnapshot::totalAsset
            ));
    }

    private BtcBenchmark buildBtcBenchmark(List<AssetSnapshot> snapshots,
                                            List<LocalDate> snapshotDates,
                                            String currency) {
        LocalDate startDate = snapshotDates.getFirst();
        LocalDate endDate = snapshotDates.getLast();

        List<BtcDailyPrice> btcPrices = btcPriceHistoryPort.findBtcDailyPrices(startDate, endDate, currency);
        Map<LocalDate, BigDecimal> priceMap = btcPrices.stream()
            .collect(Collectors.toMap(BtcDailyPrice::date, BtcDailyPrice::closePrice));

        BigDecimal seedMoney = snapshots.getFirst().totalAsset();
        return BtcBenchmark.calculate(seedMoney, priceMap, snapshotDates, startDate);
    }

    private List<ChartDataPoint> mapToAssetHistory(List<AssetSnapshot> snapshots,
                                                    CumulativeLossTimeline lossTimeline,
                                                    BtcBenchmark btcBenchmark) {
        return snapshots.stream()
            .map(snapshot -> {
                LocalDate date = snapshot.snapshotDate().toLocalDate();
                BigDecimal actualAsset = snapshot.totalAsset();
                return new ChartDataPoint(
                    date,
                    actualAsset,
                    lossTimeline.calculateRuleFollowedAsset(actualAsset, date),
                    btcBenchmark.getAssetValueAt(date)
                );
            })
            .toList();
    }

    private List<ViolationMarkerPoint> mapToViolationMarkerPoints(ViolationMarkers violationMarkers) {
        return violationMarkers.getMarkers().stream()
            .map(marker -> new ViolationMarkerPoint(marker.date(), marker.assetValue()))
            .toList();
    }

    private int calculateTotalDays(List<AssetSnapshot> snapshots) {
        LocalDate first = snapshots.getFirst().snapshotDate().toLocalDate();
        LocalDate last = snapshots.getLast().snapshotDate().toLocalDate();
        return (int) ChronoUnit.DAYS.between(first, last) + 1;
    }
}
