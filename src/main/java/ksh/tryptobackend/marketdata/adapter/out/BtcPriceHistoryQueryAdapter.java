package ksh.tryptobackend.marketdata.adapter.out;

import ksh.tryptobackend.marketdata.application.port.out.BtcPriceHistoryQueryPort;
import ksh.tryptobackend.marketdata.domain.vo.DailyClosePrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BtcPriceHistoryQueryAdapter implements BtcPriceHistoryQueryPort {

    // TODO: InfluxDB 클라이언트 주입 후 실제 구현
    @Override
    public List<DailyClosePrice> findBtcDailyPrices(LocalDate startDate, LocalDate endDate, String currency) {
        throw new UnsupportedOperationException("InfluxDB 연동 후 구현 예정");
    }
}
