package ksh.scheduler.compensation.detector;

import ksh.scheduler.compensation.entity.Side;
import ksh.scheduler.compensation.executor.FillExecutor;
import ksh.scheduler.compensation.model.PendingOrder;
import ksh.scheduler.compensation.model.Tick;
import ksh.scheduler.compensation.repository.InfluxTickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissedFillDetector {

    private final InfluxTickRepository influxRepo;
    private final FillExecutor fillExecutor;

    public boolean tryCompensate(PendingOrder order) {
        Instant from = order.createdAt().atZone(ZoneId.systemDefault()).toInstant();
        Instant to = Instant.now();

        String symbol = order.displayName() + "/KRW";
        List<Tick> ticks = influxRepo.findTicks(order.exchangeName(), symbol, from, to);
        BigDecimal orderPrice = order.price();
        boolean buy = order.side() == Side.BUY;
        for (Tick t : ticks) {
            if (buy ? t.price().compareTo(orderPrice) <= 0 : t.price().compareTo(orderPrice) >= 0) {
                return fillExecutor.executeFill(order, t.price(), t.time());
            }
        }
        return false;
    }
}
