package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.TickResult;

import java.time.Instant;
import java.util.List;

public interface FindTicksUseCase {

    List<TickResult> findTicks(String exchangeName, String marketSymbol, Instant from, Instant to);
}
