package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.ranking.application.port.out.RankingEligibilityPort;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("rankingEligibilityAdapter")
@RequiredArgsConstructor
public class RankingEligibilityAdapter implements RankingEligibilityPort {

    private final OrderQueryPort orderQueryPort;

    @Override
    public boolean hasFilledOrders(Long walletId) {
        return orderQueryPort.existsFilledByWalletId(walletId);
    }
}
