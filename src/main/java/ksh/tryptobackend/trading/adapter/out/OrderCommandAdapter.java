package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.trading.adapter.out.entity.OrderJpaEntity;
import ksh.tryptobackend.trading.adapter.out.repository.OrderJpaRepository;
import ksh.tryptobackend.trading.application.port.out.OrderCommandPort;
import ksh.tryptobackend.trading.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderCommandAdapter implements OrderCommandPort {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.fromDomain(order);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId)
            .map(OrderJpaEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return orderJpaRepository.findByIdempotencyKey(idempotencyKey)
            .map(OrderJpaEntity::toDomain);
    }

    @Override
    public long countByWalletIdAndCreatedAtBetween(Long walletId, LocalDateTime from, LocalDateTime to) {
        return orderJpaRepository.countByWalletIdAndCreatedAtBetween(walletId, from, to);
    }
}
