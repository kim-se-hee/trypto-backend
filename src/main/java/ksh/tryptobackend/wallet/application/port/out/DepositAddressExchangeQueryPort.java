package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.domain.vo.DepositTargetExchange;

public interface DepositAddressExchangeQueryPort {

    DepositTargetExchange getExchange(Long exchangeId);
}
