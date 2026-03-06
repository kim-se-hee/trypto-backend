package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;
import ksh.tryptobackend.ranking.application.port.out.ExchangeInfoQueryPort;
import ksh.tryptobackend.ranking.application.port.out.dto.ExchangeSnapshotInfo;
import ksh.tryptobackend.ranking.domain.vo.KrwConversionRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("rankingExchangeInfoSnapshotAdapter")
@RequiredArgsConstructor
public class ExchangeInfoSnapshotAdapter implements ExchangeInfoQueryPort {

    private final FindExchangeDetailUseCase findExchangeDetailUseCase;

    @Override
    public ExchangeSnapshotInfo getExchangeInfo(Long exchangeId) {
        ExchangeDetailResult detail = findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
        KrwConversionRate rate = detail.domestic() ? KrwConversionRate.DOMESTIC : KrwConversionRate.OVERSEAS;
        return new ExchangeSnapshotInfo(exchangeId, detail.baseCurrencyCoinId(), rate);
    }
}
