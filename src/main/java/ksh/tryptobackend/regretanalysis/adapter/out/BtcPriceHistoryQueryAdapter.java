package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.regretanalysis.application.port.out.BtcPriceHistoryQueryPort;
import ksh.tryptobackend.regretanalysis.domain.vo.BtcDailyPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BtcPriceHistoryQueryAdapter implements BtcPriceHistoryQueryPort {

    // TODO: InfluxDB 클라이언트 주입 후 실제 구현
    @Override
    public List<BtcDailyPrice> findBtcDailyPrices(LocalDate startDate, LocalDate endDate, String currency) {
        throw new UnsupportedOperationException("InfluxDB 연동 후 구현 예정");
    }
}
