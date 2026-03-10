package ksh.tryptobackend.portfolio.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.investmentround.application.port.in.FindRoundInfoUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.RoundInfoResult;
import ksh.tryptobackend.marketdata.application.port.in.FindCoinInfoUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.GetLivePriceUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.CoinInfoResult;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;
import ksh.tryptobackend.portfolio.application.port.in.GetMyHoldingsUseCase;
import ksh.tryptobackend.portfolio.application.port.in.dto.query.GetMyHoldingsQuery;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.HoldingSnapshotResult;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.MyHoldingsResult;
import ksh.tryptobackend.trading.application.port.in.FindActiveHoldingsUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.result.HoldingInfoResult;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import ksh.tryptobackend.wallet.application.port.in.GetAvailableBalanceUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyHoldingsService implements GetMyHoldingsUseCase {

    private final FindWalletUseCase findWalletUseCase;
    private final FindRoundInfoUseCase findRoundInfoUseCase;
    private final FindExchangeDetailUseCase findExchangeDetailUseCase;
    private final GetAvailableBalanceUseCase getAvailableBalanceUseCase;
    private final FindActiveHoldingsUseCase findActiveHoldingsUseCase;
    private final FindCoinInfoUseCase findCoinInfoUseCase;
    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;
    private final GetLivePriceUseCase getLivePriceUseCase;

    @Override
    public MyHoldingsResult getMyHoldings(GetMyHoldingsQuery query) {
        WalletResult wallet = findWallet(query.walletId());
        verifyOwnership(wallet.roundId(), query.userId());
        ExchangeDetailResult exchange = findExchangeDetail(wallet.exchangeId());
        BigDecimal baseCurrencyBalance = getAvailableBalanceUseCase.getAvailableBalance(
                query.walletId(), exchange.baseCurrencyCoinId());
        List<HoldingInfoResult> holdings = findActiveHoldingsUseCase.findActiveHoldings(query.walletId());
        List<HoldingSnapshotResult> holdingSnapshots = buildHoldingSnapshots(
                holdings, wallet.exchangeId(), exchange.baseCurrencyCoinId());
        String baseCurrencySymbol = findBaseCurrencySymbol(exchange.baseCurrencyCoinId());

        return new MyHoldingsResult(wallet.exchangeId(), baseCurrencyBalance, baseCurrencySymbol, holdingSnapshots);
    }

    private WalletResult findWallet(Long walletId) {
        return findWalletUseCase.findById(walletId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
    }

    private void verifyOwnership(Long roundId, Long userId) {
        RoundInfoResult round = findRoundInfoUseCase.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
        if (!round.userId().equals(userId)) {
            throw new CustomException(ErrorCode.WALLET_NOT_OWNED);
        }
    }

    private ExchangeDetailResult findExchangeDetail(Long exchangeId) {
        return findExchangeDetailUseCase.findExchangeDetail(exchangeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }

    private String findBaseCurrencySymbol(Long baseCurrencyCoinId) {
        Map<Long, CoinInfoResult> coinInfoMap = findCoinInfoUseCase.findByIds(Set.of(baseCurrencyCoinId));
        CoinInfoResult coinInfo = coinInfoMap.get(baseCurrencyCoinId);
        if (coinInfo == null) {
            throw new CustomException(ErrorCode.COIN_NOT_FOUND);
        }
        return coinInfo.symbol();
    }

    private List<HoldingSnapshotResult> buildHoldingSnapshots(
            List<HoldingInfoResult> holdings, Long exchangeId, Long baseCurrencyCoinId) {
        if (holdings.isEmpty()) {
            return List.of();
        }

        Set<Long> coinIds = collectCoinIds(holdings);
        Map<Long, CoinInfoResult> coinInfoMap = findCoinInfoUseCase.findByIds(coinIds);
        List<Long> coinIdList = holdings.stream().map(HoldingInfoResult::coinId).toList();
        Map<Long, Long> exchangeCoinIdMap = findExchangeCoinMappingUseCase.findExchangeCoinIdMap(exchangeId, coinIdList);

        return holdings.stream()
                .map(holding -> toHoldingSnapshot(holding, coinInfoMap, exchangeCoinIdMap))
                .toList();
    }

    private Set<Long> collectCoinIds(List<HoldingInfoResult> holdings) {
        Set<Long> coinIds = new HashSet<>();
        for (HoldingInfoResult holding : holdings) {
            coinIds.add(holding.coinId());
        }
        return coinIds;
    }

    private HoldingSnapshotResult toHoldingSnapshot(
            HoldingInfoResult holding,
            Map<Long, CoinInfoResult> coinInfoMap,
            Map<Long, Long> exchangeCoinIdMap) {
        CoinInfoResult coinInfo = coinInfoMap.get(holding.coinId());
        Long exchangeCoinId = exchangeCoinIdMap.get(holding.coinId());
        BigDecimal currentPrice = getLivePriceUseCase.getCurrentPrice(exchangeCoinId);

        return new HoldingSnapshotResult(
                holding.coinId(),
                coinInfo != null ? coinInfo.symbol() : null,
                coinInfo != null ? coinInfo.name() : null,
                holding.totalQuantity(),
                holding.avgBuyPrice(),
                currentPrice
        );
    }
}
