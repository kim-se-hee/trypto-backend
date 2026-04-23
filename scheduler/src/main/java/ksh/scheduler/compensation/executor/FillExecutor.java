package ksh.scheduler.compensation.executor;

import ksh.scheduler.compensation.holding.HoldingRecalculator;
import ksh.scheduler.compensation.model.PendingOrder;
import ksh.scheduler.compensation.repository.OrderJpaRepository;
import ksh.scheduler.compensation.repository.WalletBalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class FillExecutor {

    private final OrderJpaRepository orderJpaRepository;
    private final WalletBalanceJpaRepository walletBalanceJpaRepository;
    private final HoldingRecalculator holdingRecalculator;

    @Transactional
    public boolean executeFill(PendingOrder order, BigDecimal executedPrice, Instant executedAt) {
        LocalDateTime filledAt = LocalDateTime.ofInstant(executedAt, ZoneId.systemDefault());
        boolean filled = orderJpaRepository.fillIfPending(order.orderId(), executedPrice, filledAt);
        if (!filled) {
            return false;
        }
        walletBalanceJpaRepository.decreaseLocked(order.walletId(), order.lockedCoinId(), order.lockedAmount());
        holdingRecalculator.recalculate(order.walletId(), order.coinId());
        log.info("compensation filled orderId={} at {}", order.orderId(), executedPrice);
        return true;
    }
}
