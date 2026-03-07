package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinChainUseCase;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeCoinChainQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositAddressExchangeCoinChainQueryAdapter implements DepositAddressExchangeCoinChainQueryPort {

    private final FindExchangeCoinChainUseCase findExchangeCoinChainUseCase;

    @Override
    public boolean isTagRequired(Long exchangeId, Long coinId, String chain) {
        return findExchangeCoinChainUseCase.findByExchangeIdAndCoinIdAndChain(exchangeId, coinId, chain)
            .map(result -> result.tagRequired())
            .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_CHAIN));
    }
}
