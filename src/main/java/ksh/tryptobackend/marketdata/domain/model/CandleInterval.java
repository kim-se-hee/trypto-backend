package ksh.tryptobackend.marketdata.domain.model;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CandleInterval {

    ONE_MINUTE("1m", "candle_1m"),
    ONE_HOUR("1h", "candle_1h"),
    FOUR_HOURS("4h", "candle_4h"),
    ONE_DAY("1d", "candle_1d"),
    ONE_WEEK("1w", "candle_1w"),
    ONE_MONTH("1M", "candle_1M");

    private final String code;
    private final String measurement;

    public static CandleInterval of(String code) {
        return Arrays.stream(values())
            .filter(interval -> interval.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CANDLE_INTERVAL));
    }
}
