package ksh.tryptobackend.transfer.adapter.out;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinChainUseCase;
import ksh.tryptobackend.transfer.application.port.out.TransferExchangeCoinChainQueryPort;
import ksh.tryptobackend.transfer.domain.vo.TransferDestinationChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferExchangeCoinChainQueryAdapter implements TransferExchangeCoinChainQueryPort {

    private final FindExchangeCoinChainUseCase findExchangeCoinChainUseCase;

    @Override
    public Optional<TransferDestinationChain> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain) {
        return findExchangeCoinChainUseCase.findByExchangeIdAndCoinIdAndChain(exchangeId, coinId, chain)
            .map(result -> new TransferDestinationChain(result.tagRequired()));
    }
}
