package ksh.tryptobackend.ranking.application.port.out;

import java.util.Optional;

public interface ExchangeCoinQueryPort {

    Optional<Long> findExchangeCoinId(Long exchangeId, Long coinId);
}
