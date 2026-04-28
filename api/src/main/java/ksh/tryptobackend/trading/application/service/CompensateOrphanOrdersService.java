package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindMarketSymbolsByIdsUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindTicksUseCase;
import ksh.tryptobackend.marketdata.domain.vo.ExchangeSymbolKey;
import ksh.tryptobackend.marketdata.domain.vo.MarketSymbols;
import ksh.tryptobackend.marketdata.domain.vo.Tick;
import ksh.tryptobackend.trading.application.port.in.CompensateOrphanOrdersUseCase;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import ksh.tryptobackend.trading.domain.vo.OrphanOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensateOrphanOrdersService implements CompensateOrphanOrdersUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final OrderQueryPort orderQueryPort;

    private final FindMarketSymbolsByIdsUseCase findMarketSymbolsByIdsUseCase;
    private final FindTicksUseCase findTicksUseCase;

    private final OrphanOrderCompensator orphanOrderCompensator;

    private final Clock clock;

    @Override
    public int compensate(int boundarySeconds) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<OrphanOrder> orphans = orderQueryPort.findOrphanOrders(now.minusSeconds(boundarySeconds));
        if (orphans.isEmpty()) {
            return 0;
        }

        MarketSymbols symbols = findMarketSymbolsByIdsUseCase.findByIds(collectExchangeCoinIds(orphans));
        Instant nowInstant = now.atZone(KST).toInstant();

        int filled = 0;
        for (OrphanOrder orphan : orphans) {
            if (compensateOne(orphan, symbols, nowInstant)) {
                filled++;
            }
        }
        return filled;
    }

    private Set<Long> collectExchangeCoinIds(List<OrphanOrder> orphans) {
        return orphans.stream()
            .map(OrphanOrder::exchangeCoinId)
            .collect(Collectors.toSet());
    }

    private boolean compensateOne(OrphanOrder orphan, MarketSymbols symbols, Instant nowInstant) {
        try {
            ExchangeSymbolKey key = symbols.require(orphan.exchangeCoinId());
            Instant from = orphan.createdAt().atZone(KST).toInstant();
            List<Tick> ticks = findTicksUseCase.findTicks(key, from, nowInstant);
            for (Tick tick : ticks) {
                if (orphan.matches(tick.price())) {
                    return orphanOrderCompensator.compensate(orphan, tick);
                }
            }
            return false;
        } catch (Exception e) {
            log.error("orphan {} 보상 실패", orphan.orderId(), e);
            return false;
        }
    }
}
