package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.marketdata.domain.vo.Tick;
import ksh.tryptobackend.trading.application.port.in.RecalculateHoldingUseCase;
import ksh.tryptobackend.trading.application.port.out.OrderCommandPort;
import ksh.tryptobackend.trading.domain.vo.OrphanOrder;
import ksh.tryptobackend.wallet.application.port.in.ManageWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanOrderCompensator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final OrderCommandPort orderCommandPort;

    private final ManageWalletBalanceUseCase manageWalletBalanceUseCase;
    private final RecalculateHoldingUseCase recalculateHoldingUseCase;

    @Transactional
    public boolean compensate(OrphanOrder orphan, Tick matchedTick) {
        LocalDateTime filledAt = LocalDateTime.ofInstant(matchedTick.time(), KST);
        boolean filled = orderCommandPort.fillOrder(orphan.orderId(), matchedTick.price(), filledAt);
        if (!filled) {
            return false;
        }
        manageWalletBalanceUseCase.unlockBalance(orphan.walletId(), orphan.lockedCoinId(), orphan.lockedAmount());
        recalculateHoldingUseCase.recalculate(orphan.walletId(), orphan.coinId());
        log.info("orphan {} 보상 완료 at {}", orphan.orderId(), matchedTick.price());
        return true;
    }
}
