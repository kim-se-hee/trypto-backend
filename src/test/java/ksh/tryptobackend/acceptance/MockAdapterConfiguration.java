package ksh.tryptobackend.acceptance;

import ksh.tryptobackend.acceptance.mock.MockBtcPriceHistoryAdapter;
import ksh.tryptobackend.acceptance.mock.MockDepositAddressExchangeAdapter;
import ksh.tryptobackend.acceptance.mock.MockDepositAddressExchangeCoinChainAdapter;
import ksh.tryptobackend.acceptance.mock.MockHoldingAdapter;
import ksh.tryptobackend.acceptance.mock.MockListedCoinAdapter;
import ksh.tryptobackend.acceptance.mock.MockLivePriceAdapter;
import ksh.tryptobackend.acceptance.mock.MockPriceChangeRateAdapter;
import ksh.tryptobackend.acceptance.mock.MockViolationRuleAdapter;
import ksh.tryptobackend.acceptance.mock.MockTradingVenueAdapter;
import ksh.tryptobackend.acceptance.mock.MockWalletBalanceAdapter;
import ksh.tryptobackend.acceptance.mock.MockTransferWalletAdapter;
import ksh.tryptobackend.regretanalysis.application.port.out.BtcPriceHistoryQueryPort;
import ksh.tryptobackend.trading.application.port.out.ListedCoinQueryPort;
import ksh.tryptobackend.trading.application.port.out.ViolationRuleQueryPort;
import ksh.tryptobackend.trading.application.port.out.LivePriceQueryPort;
import ksh.tryptobackend.trading.application.port.out.PriceChangeRatePort;
import ksh.tryptobackend.trading.application.port.out.TradingVenueQueryPort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeCoinChainQueryPort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeQueryPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockAdapterConfiguration {

    @Bean
    @Primary
    public MockWalletBalanceAdapter mockWalletBalanceAdapter() {
        return new MockWalletBalanceAdapter();
    }

    @Bean
    @Primary
    public LivePriceQueryPort livePriceQueryPort() {
        return new MockLivePriceAdapter();
    }

    @Bean
    @Primary
    public TradingVenueQueryPort tradingVenueQueryPort() {
        return new MockTradingVenueAdapter();
    }

    @Bean
    @Primary
    public ListedCoinQueryPort listedCoinQueryPort() {
        return new MockListedCoinAdapter();
    }

    @Bean
    @Primary
    public MockHoldingAdapter mockHoldingAdapter() {
        return new MockHoldingAdapter();
    }

    @Bean
    @Primary
    public ViolationRuleQueryPort violationRuleQueryPort() {
        return new MockViolationRuleAdapter();
    }

    @Bean
    @Primary
    public PriceChangeRatePort priceChangeRatePort() {
        return new MockPriceChangeRateAdapter();
    }

    @Bean
    @Primary
    public BtcPriceHistoryQueryPort btcPriceHistoryPort() {
        return new MockBtcPriceHistoryAdapter();
    }

    @Bean
    @Primary
    public DepositAddressExchangeQueryPort depositAddressExchangeQueryPort() {
        return new MockDepositAddressExchangeAdapter();
    }

    @Bean
    @Primary
    public DepositAddressExchangeCoinChainQueryPort depositAddressExchangeCoinChainQueryPort() {
        return new MockDepositAddressExchangeCoinChainAdapter();
    }

    @Bean
    @Primary
    public MockTransferWalletAdapter mockTransferWalletAdapter() {
        return new MockTransferWalletAdapter();
    }
}
