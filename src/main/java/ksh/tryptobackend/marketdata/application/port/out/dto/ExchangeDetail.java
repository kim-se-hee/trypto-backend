package ksh.tryptobackend.marketdata.application.port.out.dto;

import ksh.tryptobackend.marketdata.domain.model.ExchangeMarketType;

public record ExchangeDetail(String name, Long baseCurrencyCoinId, ExchangeMarketType marketType) {

    public String currency() {
        return switch (marketType) {
            case DOMESTIC -> "KRW";
            case OVERSEAS -> "USDT";
        };
    }
}
