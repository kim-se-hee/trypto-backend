package ksh.scheduler.compensation.repository;

import ksh.scheduler.compensation.entity.OrderJpaEntity;
import ksh.scheduler.compensation.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository
    extends JpaRepository<OrderJpaEntity, Long>, OrderQueryRepository {

    List<OrderJpaEntity> findByWalletIdAndCoinIdAndStatusOrderByFilledAtAscIdAsc(
        Long walletId, Long coinId, OrderStatus status);
}
