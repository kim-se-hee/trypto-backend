package ksh.tryptobackend.transfer.adapter.in.dto.response;

import ksh.tryptobackend.transfer.domain.model.Transfer;
import ksh.tryptobackend.transfer.domain.vo.TransferFailureReason;
import ksh.tryptobackend.transfer.domain.vo.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCoinResponse(
    TransferStatus status,
    BigDecimal fee,
    TransferFailureReason failureReason,
    LocalDateTime frozenUntil
) {

    public static TransferCoinResponse from(Transfer transfer) {
        return new TransferCoinResponse(
            transfer.getStatus(),
            transfer.getFee(),
            transfer.getFailureReason(),
            transfer.getFrozenUntil()
        );
    }
}
