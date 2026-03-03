package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.ranking.application.port.out.TradeCountPort;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("rankingTradeCountAdapter")
@RequiredArgsConstructor
public class TradeCountAdapter implements TradeCountPort {

    private final OrderQueryPort orderQueryPort;

    @Override
    public int countFilledOrders(Long walletId) {
        return orderQueryPort.countFilledByWalletId(walletId);
    }
}
