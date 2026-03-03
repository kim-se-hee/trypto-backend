package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeQueryPort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangePort;
import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressExchangeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositAddressExchangeAdapter implements DepositAddressExchangePort {

    private final ExchangeQueryPort exchangeQueryPort;

    @Override
    public DepositAddressExchangeInfo getExchangeDetail(Long exchangeId) {
        return exchangeQueryPort.findExchangeDetailById(exchangeId)
            .map(detail -> new DepositAddressExchangeInfo(detail.baseCurrencyCoinId(), detail.currency()))
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }
}
