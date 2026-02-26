package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.trading.application.port.out.WalletBalancePort;
import ksh.tryptobackend.wallet.domain.model.WalletBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletBalanceJpaPersistenceAdapter implements WalletBalancePort {

    private final WalletBalanceJpaRepository repository;

    @Override
    public BigDecimal getAvailableBalance(Long walletId, Long coinId) {
        return repository.findByWalletIdAndCoinId(walletId, coinId)
            .map(WalletBalanceJpaEntity::getAvailable)
            .orElse(BigDecimal.ZERO);
    }

    @Override
    public void deductBalance(Long walletId, Long coinId, BigDecimal amount) {
        WalletBalanceJpaEntity entity = getOrCreateEntity(walletId, coinId);
        WalletBalance domain = entity.toDomain();
        domain.deductAvailable(amount);
        entity.updateFrom(domain);
    }

    @Override
    public void addBalance(Long walletId, Long coinId, BigDecimal amount) {
        WalletBalanceJpaEntity entity = getOrCreateEntity(walletId, coinId);
        WalletBalance domain = entity.toDomain();
        domain.addAvailable(amount);
        entity.updateFrom(domain);
    }

    @Override
    public void lockBalance(Long walletId, Long coinId, BigDecimal amount) {
        WalletBalanceJpaEntity entity = getOrCreateEntity(walletId, coinId);
        WalletBalance domain = entity.toDomain();
        domain.lock(amount);
        entity.updateFrom(domain);
    }

    @Override
    public void unlockBalance(Long walletId, Long coinId, BigDecimal amount) {
        WalletBalanceJpaEntity entity = getOrCreateEntity(walletId, coinId);
        WalletBalance domain = entity.toDomain();
        domain.unlock(amount);
        entity.updateFrom(domain);
    }

    private WalletBalanceJpaEntity getOrCreateEntity(Long walletId, Long coinId) {
        return repository.findByWalletIdAndCoinId(walletId, coinId)
            .orElseGet(() -> {
                WalletBalance newBalance = WalletBalance.builder()
                    .walletId(walletId)
                    .coinId(coinId)
                    .available(BigDecimal.ZERO)
                    .locked(BigDecimal.ZERO)
                    .build();
                return repository.save(WalletBalanceJpaEntity.fromDomain(newBalance));
            });
    }
}
