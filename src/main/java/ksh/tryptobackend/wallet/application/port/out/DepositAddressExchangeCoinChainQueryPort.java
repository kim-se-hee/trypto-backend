package ksh.tryptobackend.wallet.application.port.out;

public interface DepositAddressExchangeCoinChainQueryPort {

    boolean isTagRequired(Long exchangeId, Long coinId, String chain);
}
