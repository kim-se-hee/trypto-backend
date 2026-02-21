package ksh.tryptobackend.trading.adapter.in.dto.response;

import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.vo.OrderType;
import ksh.tryptobackend.trading.domain.vo.Side;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderHistoryResponse(
        Long orderId,
        Long exchangeCoinId,
        Side side,
        OrderType orderType,
        BigDecimal filledPrice,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal orderAmount,
        BigDecimal fee,
        LocalDateTime createdAt,
        LocalDateTime filledAt
) {

    public static OrderHistoryResponse from(Order order) {
        return new OrderHistoryResponse(
                order.getId(),
                order.getExchangeCoinId(),
                order.getSide(),
                order.getOrderType(),
                order.getFilledPrice(),
                order.getPrice(),
                order.getQuantity(),
                order.getOrderAmount(),
                order.getFee() != null ? order.getFee().getAmount() : null,
                order.getCreatedAt(),
                order.getFilledAt()
        );
    }
}
