package ksh.tryptobackend.trading.application.port.out;

import ksh.tryptobackend.trading.domain.model.ViolationRule;

import java.util.List;

public interface ViolationRuleQueryPort {

    List<ViolationRule> findByWalletId(Long walletId);
}
