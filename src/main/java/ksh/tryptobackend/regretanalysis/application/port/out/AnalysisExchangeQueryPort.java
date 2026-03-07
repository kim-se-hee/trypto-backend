package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisExchange;

public interface AnalysisExchangeQueryPort {

    AnalysisExchange getExchangeInfo(Long exchangeId);

    boolean existsWalletForExchange(Long roundId, Long exchangeId);
}
