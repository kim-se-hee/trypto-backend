package ksh.tryptobackend.ranking.application.port.out.dto;

import java.time.LocalDateTime;

public record ActiveRoundInfo(Long roundId, Long userId, LocalDateTime startedAt) {
}
