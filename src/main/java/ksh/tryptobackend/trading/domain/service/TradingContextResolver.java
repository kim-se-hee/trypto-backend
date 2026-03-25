package ksh.tryptobackend.trading.domain.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.GetLivePriceUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.command.PlaceOrderCommand;
import ksh.tryptobackend.trading.domain.vo.OrderMode;
import ksh.tryptobackend.trading.domain.vo.TradingContext;
import ksh.tryptobackend.trading.domain.vo.TradingVenue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TradingContextResolver {

    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;
    private final FindExchangeDetailUseCase findExchangeDetailUseCase;
    private final GetLivePriceUseCase getLivePriceUseCase;

    private final Clock clock;

    public TradingContext resolve(PlaceOrderCommand cmd) {
        var mapping = findExchangeCoinMappingUseCase.findById(cmd.exchangeCoinId())
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_COIN_NOT_FOUND));

        TradingVenue venue = findExchangeDetailUseCase.findExchangeDetail(mapping.exchangeId())
            .map(d -> TradingVenue.of(d.feeRate(), d.baseCurrencyCoinId(), d.domestic()))
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));

        BigDecimal currentPrice = getLivePriceUseCase.getCurrentPrice(cmd.exchangeCoinId());
        OrderMode mode = OrderMode.of(cmd.orderType(), cmd.side());

        return new TradingContext(mapping.coinId(), venue, mode, currentPrice, LocalDateTime.now(clock));
    }
}
