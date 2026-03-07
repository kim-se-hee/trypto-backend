package ksh.tryptobackend.transfer.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.transfer.application.port.out.TransferExchangeQueryPort;
import ksh.tryptobackend.transfer.domain.vo.TransferSourceExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferExchangeQueryAdapter implements TransferExchangeQueryPort {

    private final FindExchangeDetailUseCase findExchangeDetailUseCase;

    @Override
    public TransferSourceExchange getExchangeDetail(Long exchangeId) {
        return findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .map(detail -> new TransferSourceExchange(
                detail.baseCurrencyCoinId(), detail.domestic()))
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }
}
