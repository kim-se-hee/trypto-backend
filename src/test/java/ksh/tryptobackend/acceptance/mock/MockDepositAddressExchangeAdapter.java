package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeQueryPort;
import ksh.tryptobackend.wallet.domain.vo.DepositTargetExchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockDepositAddressExchangeAdapter implements DepositAddressExchangeQueryPort {

    private final Map<Long, DepositTargetExchange> exchanges = new ConcurrentHashMap<>();

    @Override
    public DepositTargetExchange getExchange(Long exchangeId) {
        DepositTargetExchange exchange = exchanges.get(exchangeId);
        if (exchange == null) {
            throw new CustomException(ErrorCode.EXCHANGE_NOT_FOUND);
        }
        return exchange;
    }

    public void addExchange(Long exchangeId, DepositTargetExchange exchange) {
        exchanges.put(exchangeId, exchange);
    }

    public void clear() {
        exchanges.clear();
    }
}
