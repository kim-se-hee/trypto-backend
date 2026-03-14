package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.application.port.out.dto.ExchangeDetail;
import ksh.tryptobackend.marketdata.application.port.out.dto.ExchangeSummary;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ExchangeQueryPort {

    Optional<ExchangeDetail> findExchangeDetailById(Long exchangeId);

    Optional<ExchangeSummary> findExchangeSummaryById(Long exchangeId);

    List<Long> findAllExchangeIds();

    Map<Long, String> findNamesByIds(Set<Long> exchangeIds);
}
