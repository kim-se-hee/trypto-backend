package ksh.tryptobackend.ranking.application.port.out;

import ksh.tryptobackend.ranking.application.port.out.dto.HoldingInfo;

import java.util.List;

public interface HoldingQueryPort {

    List<HoldingInfo> findAllByWalletId(Long walletId);
}
