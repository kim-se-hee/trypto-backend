package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressChainInfo;

public interface DepositAddressExchangeCoinChainQueryPort {

    DepositAddressChainInfo getExchangeCoinChain(Long exchangeId, Long coinId, String chain);
}
