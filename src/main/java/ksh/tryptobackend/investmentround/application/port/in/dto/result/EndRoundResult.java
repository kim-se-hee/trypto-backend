package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import ksh.tryptobackend.investmentround.domain.vo.RoundStatus;

import java.time.LocalDateTime;

public record EndRoundResult(Long roundId, RoundStatus status, LocalDateTime endedAt) {
}
