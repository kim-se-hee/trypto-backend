package ksh.tryptobackend.transfer.application.port.out;

import ksh.tryptobackend.transfer.domain.vo.TransferDestinationChain;

import java.util.Optional;

public interface TransferExchangeCoinChainQueryPort {

    Optional<TransferDestinationChain> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
