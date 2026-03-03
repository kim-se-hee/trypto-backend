package ksh.tryptobackend.ranking.application.port.out;

public interface RankingEligibilityPort {

    boolean hasFilledOrders(Long walletId);
}
