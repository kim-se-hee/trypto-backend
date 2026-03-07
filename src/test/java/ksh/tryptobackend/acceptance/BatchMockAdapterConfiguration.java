package ksh.tryptobackend.acceptance;

import ksh.tryptobackend.acceptance.mock.MockActiveRoundListAdapter;
import ksh.tryptobackend.acceptance.mock.MockActiveRoundQueryAdapter;
import ksh.tryptobackend.acceptance.mock.MockBalanceQueryAdapter;
import ksh.tryptobackend.acceptance.mock.MockEligibleRoundQueryAdapter;
import ksh.tryptobackend.acceptance.mock.MockEmergencyFundingSnapshotAdapter;
import ksh.tryptobackend.acceptance.mock.MockExchangeInfoQueryAdapter;
import ksh.tryptobackend.acceptance.mock.MockSnapshotHoldingQueryAdapter;
import ksh.tryptobackend.acceptance.mock.MockRankingLivePriceAdapter;
import ksh.tryptobackend.acceptance.mock.MockTradeViolationQueryAdapter;
import ksh.tryptobackend.acceptance.mock.MockWalletSnapshotAdapter;
import ksh.tryptobackend.ranking.application.port.out.ActiveRoundQueryPort;
import ksh.tryptobackend.ranking.application.port.out.BalanceQueryPort;
import ksh.tryptobackend.ranking.application.port.out.EligibleRoundQueryPort;
import ksh.tryptobackend.ranking.application.port.out.EmergencyFundingSnapshotQueryPort;
import ksh.tryptobackend.ranking.application.port.out.ExchangeSnapshotQueryPort;
import ksh.tryptobackend.ranking.application.port.out.EvaluatedHoldingQueryPort;
import ksh.tryptobackend.ranking.application.port.out.LivePriceQueryPort;
import ksh.tryptobackend.ranking.application.port.out.WalletSnapshotQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.ActiveRoundExchangeQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.TradeViolationQueryPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class BatchMockAdapterConfiguration {

    @Bean
    @Primary
    public ActiveRoundQueryPort activeRoundQueryPort() {
        return new MockActiveRoundQueryAdapter();
    }

    @Bean
    @Primary
    public WalletSnapshotQueryPort walletSnapshotPort() {
        return new MockWalletSnapshotAdapter();
    }

    @Bean
    @Primary
    public ExchangeSnapshotQueryPort exchangeSnapshotPort() {
        return new MockExchangeInfoQueryAdapter();
    }

    @Bean
    @Primary
    public EvaluatedHoldingQueryPort evaluatedHoldingQueryPort() {
        return new MockSnapshotHoldingQueryAdapter();
    }

    @Bean
    @Primary
    public BalanceQueryPort balanceQueryPort() {
        return new MockBalanceQueryAdapter();
    }

    @Bean
    @Primary
    public EmergencyFundingSnapshotQueryPort emergencyFundingSnapshotPort() {
        return new MockEmergencyFundingSnapshotAdapter();
    }

    @Bean
    @Primary
    public EligibleRoundQueryPort eligibleRoundQueryPort() {
        return new MockEligibleRoundQueryAdapter();
    }

    @Bean
    @Primary
    public ActiveRoundExchangeQueryPort activeRoundExchangePort() {
        return new MockActiveRoundListAdapter();
    }

    @Bean
    @Primary
    public TradeViolationQueryPort tradeViolationQueryPort() {
        return new MockTradeViolationQueryAdapter();
    }

    @Bean
    @Primary
    public LivePriceQueryPort rankingLivePriceQueryPort() {
        return new MockRankingLivePriceAdapter();
    }
}
