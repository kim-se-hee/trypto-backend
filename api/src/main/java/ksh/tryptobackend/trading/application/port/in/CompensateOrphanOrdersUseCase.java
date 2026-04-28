package ksh.tryptobackend.trading.application.port.in;

public interface CompensateOrphanOrdersUseCase {

    int compensate(int boundarySeconds);
}
