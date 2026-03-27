package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.acceptance.TestContainerConfiguration;
import ksh.tryptobackend.investmentround.adapter.out.entity.InvestmentRoundJpaEntity;
import ksh.tryptobackend.investmentround.adapter.out.repository.InvestmentRoundJpaRepository;
import ksh.tryptobackend.investmentround.domain.model.InvestmentRound;
import ksh.tryptobackend.investmentround.domain.vo.RoundStatus;
import ksh.tryptobackend.marketdata.adapter.out.entity.CoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeCoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.repository.CoinJpaRepository;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeCoinJpaRepository;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeJpaRepository;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeCoinMappingCacheCommandPort;
import ksh.tryptobackend.marketdata.domain.model.ExchangeMarketType;
import ksh.tryptobackend.marketdata.domain.vo.ExchangeCoinMapping;
import ksh.tryptobackend.marketdata.domain.vo.ExchangeSymbolKey;
import ksh.tryptobackend.trading.adapter.out.entity.HoldingJpaEntity;
import ksh.tryptobackend.trading.adapter.out.entity.OrderJpaEntity;
import ksh.tryptobackend.trading.adapter.out.repository.HoldingJpaRepository;
import ksh.tryptobackend.trading.adapter.out.repository.OrderJpaRepository;
import ksh.tryptobackend.trading.application.port.in.MatchPendingOrdersUseCase;
import ksh.tryptobackend.trading.application.port.out.PendingOrderCacheCommandPort;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.vo.Fee;
import ksh.tryptobackend.trading.domain.vo.OrderStatus;
import ksh.tryptobackend.trading.domain.vo.OrderType;
import ksh.tryptobackend.trading.domain.vo.PendingOrder;
import ksh.tryptobackend.trading.domain.vo.Quantity;
import ksh.tryptobackend.trading.domain.vo.Side;
import ksh.tryptobackend.user.adapter.out.entity.UserJpaEntity;
import ksh.tryptobackend.user.adapter.out.repository.UserJpaRepository;
import ksh.tryptobackend.user.domain.model.User;
import ksh.tryptobackend.wallet.adapter.out.entity.WalletBalanceJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.entity.WalletJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.repository.WalletBalanceJpaRepository;
import ksh.tryptobackend.wallet.adapter.out.repository.WalletJpaRepository;
import ksh.tryptobackend.wallet.domain.model.Wallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestContainerConfiguration.class, LoadTestMockConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("미체결 주문 매칭 부하 테스트")
class PendingOrderMatchingLoadTest {

    private static final String EXCHANGE = "UPBIT";
    private static final String SYMBOL = "BTC";
    private static final BigDecimal FILL_PRICE = new BigDecimal("50000000");
    private static final BigDecimal FEE_RATE = new BigDecimal("0.0005");
    private static final BigDecimal ORDER_QUANTITY = new BigDecimal("0.001");
    private static final int MAX_WALLETS = 150;

    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASURE_ITERATIONS = 100;
    private static final int CONSUMER_THREADS = 1;
    private static final double EVENTS_PER_SECOND = 241.0;

    @Autowired private MatchPendingOrdersUseCase matchPendingOrdersUseCase;
    @Autowired private PendingOrderCacheCommandPort pendingOrderCacheCommandPort;
    @Autowired private ExchangeCoinMappingCacheCommandPort exchangeCoinMappingCacheCommandPort;

    @Autowired private CoinJpaRepository coinJpaRepository;
    @Autowired private ExchangeJpaRepository exchangeJpaRepository;
    @Autowired private ExchangeCoinJpaRepository exchangeCoinJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private InvestmentRoundJpaRepository investmentRoundJpaRepository;
    @Autowired private WalletJpaRepository walletJpaRepository;
    @Autowired private WalletBalanceJpaRepository walletBalanceJpaRepository;
    @Autowired private OrderJpaRepository orderJpaRepository;
    @Autowired private HoldingJpaRepository holdingJpaRepository;

    private Long exchangeCoinId;
    private Long coinId;
    private Long baseCurrencyCoinId;
    private final List<Long> walletIds = new ArrayList<>();

    @BeforeAll
    void setUpOnce() {
        setupReferenceData();
        setupUsersAndWallets();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 20, 50, 100, 150})
    @DisplayName("이벤트당 매칭 건수(N)별 처리 시간 측정")
    void measureProcessingTime(int n) {
        List<Long> measurements = new ArrayList<>();

        for (int i = 0; i < WARMUP_ITERATIONS + MEASURE_ITERATIONS; i++) {
            createPendingOrdersAndCache(n);

            long startNanos = System.nanoTime();
            matchPendingOrdersUseCase.matchOrders(EXCHANGE, SYMBOL, FILL_PRICE);
            long elapsedNanos = System.nanoTime() - startNanos;

            if (i >= WARMUP_ITERATIONS) {
                measurements.add(elapsedNanos);
            }

            resetAfterIteration(n);
        }

        printResult(n, measurements);
    }

    private void setupReferenceData() {
        CoinJpaEntity krw = coinJpaRepository.save(new CoinJpaEntity("KRW", "Korean Won"));
        CoinJpaEntity btc = coinJpaRepository.save(new CoinJpaEntity("BTC", "Bitcoin"));
        baseCurrencyCoinId = krw.getId();
        coinId = btc.getId();

        ExchangeJpaEntity upbit = exchangeJpaRepository.save(
            new ExchangeJpaEntity(1L, "UPBIT", ExchangeMarketType.DOMESTIC, baseCurrencyCoinId, FEE_RATE));

        ExchangeCoinJpaEntity exchangeCoin = exchangeCoinJpaRepository.save(
            new ExchangeCoinJpaEntity(upbit.getId(), coinId, "BTC/KRW"));
        exchangeCoinId = exchangeCoin.getId();

        exchangeCoinMappingCacheCommandPort.loadAll(Map.of(
            new ExchangeSymbolKey(EXCHANGE, SYMBOL),
            new ExchangeCoinMapping(exchangeCoinId, upbit.getId(), coinId, "BTC")
        ));
    }

    private void setupUsersAndWallets() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal largeBalance = new BigDecimal("1000000000");

        for (int i = 0; i < MAX_WALLETS; i++) {
            User user = User.reconstitute(null,
                "loadtest" + i + "@test.com", "loadtest-user-" + i, false, now, now);
            UserJpaEntity savedUser = userJpaRepository.save(UserJpaEntity.fromDomain(user));

            InvestmentRound round = InvestmentRound.start(
                savedUser.getId(), 0, largeBalance, BigDecimal.ZERO, now);
            InvestmentRoundJpaEntity savedRound = investmentRoundJpaRepository.save(
                InvestmentRoundJpaEntity.fromDomain(round));

            Wallet wallet = Wallet.create(savedRound.getId(), 1L, largeBalance, now);
            WalletJpaEntity savedWallet = walletJpaRepository.save(WalletJpaEntity.fromDomain(wallet));

            walletBalanceJpaRepository.save(
                new WalletBalanceJpaEntity(savedWallet.getId(), baseCurrencyCoinId,
                    BigDecimal.ZERO, largeBalance));

            walletIds.add(savedWallet.getId());
        }
    }

    private void createPendingOrdersAndCache(int n) {
        BigDecimal amount = FILL_PRICE.multiply(ORDER_QUANTITY);
        BigDecimal feeAmount = amount.multiply(FEE_RATE);

        for (int i = 0; i < n; i++) {
            Long walletId = walletIds.get(i % MAX_WALLETS);
            Long orderId = savePendingBuyOrder(walletId, amount, feeAmount);

            pendingOrderCacheCommandPort.add(
                new PendingOrder(orderId, exchangeCoinId, Side.BUY, FILL_PRICE));
        }
    }

    private Long savePendingBuyOrder(Long walletId, BigDecimal amount, BigDecimal feeAmount) {
        Order order = Order.reconstitute(
            null, "load-test-" + System.nanoTime(), walletId, exchangeCoinId,
            Side.BUY, OrderType.LIMIT,
            amount, new Quantity(ORDER_QUANTITY),
            FILL_PRICE, FILL_PRICE,
            Fee.of(feeAmount, FEE_RATE),
            OrderStatus.PENDING, null,
            LocalDateTime.now(), null, null);
        OrderJpaEntity entity = OrderJpaEntity.fromDomain(order);
        return orderJpaRepository.save(entity).getId();
    }

    private void resetAfterIteration(int n) {
        BigDecimal largeBalance = new BigDecimal("1000000000");
        for (int i = 0; i < Math.min(n, MAX_WALLETS); i++) {
            Long walletId = walletIds.get(i);
            walletBalanceJpaRepository.findByWalletIdAndCoinId(walletId, baseCurrencyCoinId)
                .ifPresent(balance -> {
                    balance.updateFrom(
                        ksh.tryptobackend.wallet.domain.model.WalletBalance.builder()
                            .id(balance.getId())
                            .walletId(balance.getWalletId())
                            .coinId(balance.getCoinId())
                            .available(BigDecimal.ZERO)
                            .locked(largeBalance)
                            .build());
                    walletBalanceJpaRepository.save(balance);
                });

            walletBalanceJpaRepository.findByWalletIdAndCoinId(walletId, coinId)
                .ifPresent(walletBalanceJpaRepository::delete);

            holdingJpaRepository.findByWalletIdAndCoinId(walletId, coinId)
                .ifPresent(holdingJpaRepository::delete);
        }
    }

    private void printResult(int n, List<Long> nanosMeasurements) {
        double budgetMs = 1000.0 * CONSUMER_THREADS / EVENTS_PER_SECOND;

        Collections.sort(nanosMeasurements);
        int size = nanosMeasurements.size();
        double p50 = nanosToMs(nanosMeasurements.get(size / 2));
        double p95 = nanosToMs(nanosMeasurements.get((int) (size * 0.95)));
        double p99 = nanosToMs(nanosMeasurements.get(Math.min((int) (size * 0.99), size - 1)));
        double min = nanosToMs(nanosMeasurements.get(0));
        double max = nanosToMs(nanosMeasurements.get(size - 1));
        double avg = nanosMeasurements.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;

        boolean pass = p95 <= budgetMs;
        long totalFills = (long) n * size;

        System.out.println();
        System.out.println("============================================================");
        System.out.printf("  N=%-4d | 큐 안정 한계: %.1fms (C=%d, 241 events/s)%n", n, budgetMs, CONSUMER_THREADS);
        System.out.println("------------------------------------------------------------");
        System.out.printf("  T_process(N)  min: %7.1fms  avg: %7.1fms  max: %7.1fms%n", min, avg, max);
        System.out.printf("                p50: %7.1fms  p95: %7.1fms  p99: %7.1fms%n", p50, p95, p99);
        System.out.println("------------------------------------------------------------");
        System.out.printf("  체결 수: %d건  |  판정: %s%n", totalFills, pass ? "✅ PASS" : "❌ FAIL");
        System.out.println("============================================================");
    }

    private double nanosToMs(long nanos) {
        return nanos / 1_000_000.0;
    }
}
