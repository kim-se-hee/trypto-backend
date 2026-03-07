package ksh.tryptobackend.transfer.application.port.out;

import ksh.tryptobackend.transfer.domain.vo.TransferWallet;

import java.math.BigDecimal;

public interface TransferWalletQueryPort {

    Long getOwnerUserId(Long walletId);

    TransferWallet getWallet(Long walletId);

    BigDecimal getAvailableBalance(Long walletId, Long coinId);
}
