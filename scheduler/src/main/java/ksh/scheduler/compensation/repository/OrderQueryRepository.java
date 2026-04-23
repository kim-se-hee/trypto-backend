package ksh.scheduler.compensation.repository;

import ksh.scheduler.compensation.model.PendingOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderQueryRepository {

    List<PendingOrder> findBoundaryPending(int boundarySeconds);

    boolean fillIfPending(Long orderId, BigDecimal filledPrice, LocalDateTime filledAt);
}
