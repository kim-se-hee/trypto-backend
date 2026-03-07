package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.regretanalysis.application.port.out.LivePricePort;
import ksh.tryptobackend.trading.application.port.in.GetLivePriceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("regretLivePriceAdapter")
@RequiredArgsConstructor
public class LivePriceAdapter implements LivePricePort {

    private final GetLivePriceUseCase getLivePriceUseCase;

    @Override
    public BigDecimal getCurrentPrice(Long exchangeCoinId) {
        return getLivePriceUseCase.getCurrentPrice(exchangeCoinId);
    }
}
