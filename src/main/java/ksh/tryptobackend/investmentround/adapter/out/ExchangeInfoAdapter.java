package ksh.tryptobackend.investmentround.adapter.out;

import ksh.tryptobackend.investmentround.application.port.out.ExchangeInfoPort;
import ksh.tryptobackend.investmentround.application.port.out.dto.ExchangeInfo;
import ksh.tryptobackend.investmentround.domain.vo.SeedAmountPolicy;
import ksh.tryptobackend.marketdata.application.port.out.ExchangePort;
import ksh.tryptobackend.marketdata.domain.model.Exchange;
import ksh.tryptobackend.marketdata.domain.model.ExchangeMarketType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeInfoAdapter implements ExchangeInfoPort {

    private final ExchangePort exchangePort;

    @Override
    public Optional<ExchangeInfo> findById(Long exchangeId) {
        return exchangePort.findById(exchangeId)
            .map(this::toExchangeInfo);
    }

    private ExchangeInfo toExchangeInfo(Exchange exchange) {
        return new ExchangeInfo(
            exchange.getBaseCurrencyCoinId(),
            toSeedAmountPolicy(exchange.getMarketType())
        );
    }

    private SeedAmountPolicy toSeedAmountPolicy(ExchangeMarketType marketType) {
        return switch (marketType) {
            case DOMESTIC -> SeedAmountPolicy.DOMESTIC;
            case OVERSEAS -> SeedAmountPolicy.OVERSEAS;
        };
    }
}
