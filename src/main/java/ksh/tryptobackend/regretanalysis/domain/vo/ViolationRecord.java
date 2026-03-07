package ksh.tryptobackend.regretanalysis.domain.vo;

import java.time.LocalDateTime;

public record ViolationRecord(
    Long violationId,
    Long orderId,
    Long ruleId,
    LocalDateTime createdAt
) {
}
