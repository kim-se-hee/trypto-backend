package ksh.tryptobackend.ranking.application.port.out.dto;

import java.math.BigDecimal;

public record HoldingInfo(Long coinId, BigDecimal avgBuyPrice, BigDecimal totalQuantity) {
}
