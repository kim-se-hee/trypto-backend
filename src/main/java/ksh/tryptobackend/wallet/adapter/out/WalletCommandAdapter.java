package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.wallet.adapter.out.entity.WalletBalanceJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.entity.WalletJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.repository.WalletBalanceJpaRepository;
import ksh.tryptobackend.wallet.adapter.out.repository.WalletJpaRepository;
import ksh.tryptobackend.wallet.application.port.out.WalletCommandPort;
import ksh.tryptobackend.wallet.domain.model.Wallet;
import ksh.tryptobackend.wallet.domain.model.WalletBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class WalletCommandAdapter implements WalletCommandPort {

    private final WalletJpaRepository walletRepository;
    private final WalletBalanceJpaRepository balanceRepository;

    @Override
    public Long createWallet(Long roundId, Long exchangeId, BigDecimal seedAmount, LocalDateTime createdAt) {
        Wallet wallet = Wallet.create(roundId, exchangeId, seedAmount, createdAt);
        WalletJpaEntity saved = walletRepository.save(WalletJpaEntity.fromDomain(wallet));
        return saved.getId();
    }

    @Override
    public Long createWalletWithBalance(Long roundId, Long exchangeId, Long baseCurrencyCoinId,
                                        BigDecimal initialAmount, LocalDateTime createdAt) {
        Long walletId = createWallet(roundId, exchangeId, initialAmount, createdAt);
        balanceRepository.save(
            new WalletBalanceJpaEntity(walletId, baseCurrencyCoinId, initialAmount, BigDecimal.ZERO));
        return walletId;
    }

    @Override
    @Transactional
    public void deductBalance(Long walletId, Long coinId, BigDecimal amount) {
        executeBalanceOperation(walletId, coinId, balance -> balance.deductAvailable(amount));
    }

    @Override
    @Transactional
    public void addBalance(Long walletId, Long coinId, BigDecimal amount) {
        executeBalanceOperation(walletId, coinId, balance -> balance.addAvailable(amount));
    }

    @Override
    @Transactional
    public void lockBalance(Long walletId, Long coinId, BigDecimal amount) {
        executeBalanceOperation(walletId, coinId, balance -> balance.lock(amount));
    }

    @Override
    @Transactional
    public void unlockBalance(Long walletId, Long coinId, BigDecimal amount) {
        executeBalanceOperation(walletId, coinId, balance -> balance.unlock(amount));
    }

    private void executeBalanceOperation(Long walletId, Long coinId, Consumer<WalletBalance> operation) {
        WalletBalanceJpaEntity entity = getOrCreateEntityForUpdate(walletId, coinId);
        WalletBalance balance = entity.toDomain();
        operation.accept(balance);
        entity.updateFrom(balance);
    }

    private WalletBalanceJpaEntity getOrCreateEntityForUpdate(Long walletId, Long coinId) {
        return balanceRepository.findForUpdateByWalletIdAndCoinId(walletId, coinId)
            .orElseGet(() -> createBalanceEntity(walletId, coinId));
    }

    private WalletBalanceJpaEntity createBalanceEntity(Long walletId, Long coinId) {
        try {
            return balanceRepository.saveAndFlush(
                new WalletBalanceJpaEntity(walletId, coinId, BigDecimal.ZERO, BigDecimal.ZERO));
        } catch (DataIntegrityViolationException e) {
            return balanceRepository.findForUpdateByWalletIdAndCoinId(walletId, coinId)
                .orElseThrow(() -> e);
        }
    }
}
