package ksh.tryptocollector.loadtest;

import java.math.BigDecimal;
import ksh.tryptocollector.distribute.TickerSinkProcessor;
import ksh.tryptocollector.model.NormalizedTicker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// k6 가 한 건씩 직접 ticker 를 푸시할 때 받는 endpoint.
// ramp profile 기반 자동 발행(SyntheticTickerGenerator) 과는 별개 — 시나리오 쪽에서
// 거래소·코인·가격을 자유롭게 컨트롤하고 싶을 때 쓴다.
@Slf4j
@RestController
@RequestMapping("/internal/loadtest/ticker")
@Profile("loadtest")
@RequiredArgsConstructor
public class LoadtestTickerIngestController {

    private final TickerSinkProcessor processor;

    @PostMapping
    public ResponseEntity<Void> ingest(@RequestBody TickerIngestRequest req) {
        NormalizedTicker ticker = new NormalizedTicker(
                req.exchange(),
                req.base(),
                req.quote(),
                req.displayName(),
                req.lastPrice(),
                req.changeRate(),
                req.quoteTurnover(),
                req.tsMs());
        processor.process(ticker);
        return ResponseEntity.accepted().build();
    }

    public record TickerIngestRequest(
            String exchange,
            String base,
            String quote,
            String displayName,
            BigDecimal lastPrice,
            BigDecimal changeRate,
            BigDecimal quoteTurnover,
            long tsMs) {}
}
