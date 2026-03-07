package ksh.tryptobackend.investmentround.application.port.out;

import ksh.tryptobackend.investmentround.application.port.out.dto.ExchangeInfo;

import java.util.Optional;

public interface ExchangeInfoQueryPort {

    Optional<ExchangeInfo> findById(Long exchangeId);
}
