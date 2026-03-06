package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;
import ksh.tryptobackend.regretanalysis.application.port.out.ExchangeInfoPort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.ExchangeInfoRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("regretExchangeInfoAdapter")
@RequiredArgsConstructor
public class ExchangeInfoAdapter implements ExchangeInfoPort {

    private final FindExchangeDetailUseCase findExchangeDetailUseCase;

    @Override
    public ExchangeInfoRecord getExchangeInfo(Long exchangeId) {
        ExchangeDetailResult detail = findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));

        return new ExchangeInfoRecord(exchangeId, detail.name(), detail.domestic() ? "KRW" : "USDT");
    }
}
