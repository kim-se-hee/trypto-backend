package ksh.tryptobackend.ranking.application.port.out;

public interface TradeCountPort {

    int countFilledOrders(Long walletId);
}
