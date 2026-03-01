package ksh.tryptobackend.investmentround.adapter.in.dto.response;

import ksh.tryptobackend.investmentround.application.port.in.dto.result.EndRoundResult;
import ksh.tryptobackend.investmentround.domain.vo.RoundStatus;

import java.time.LocalDateTime;

public record EndRoundResponse(Long roundId, RoundStatus status, LocalDateTime endedAt) {

    public static EndRoundResponse from(EndRoundResult result) {
        return new EndRoundResponse(result.roundId(), result.status(), result.endedAt());
    }
}
