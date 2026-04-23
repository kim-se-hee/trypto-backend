package ksh.scheduler.compensation.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Tick(Instant time, BigDecimal price) {
}
