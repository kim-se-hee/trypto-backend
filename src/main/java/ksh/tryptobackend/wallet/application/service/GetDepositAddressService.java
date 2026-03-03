package ksh.tryptobackend.wallet.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.wallet.application.port.in.GetDepositAddressUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.query.GetDepositAddressQuery;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeCoinChainPort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangePort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressPersistencePort;
import ksh.tryptobackend.wallet.application.port.out.WalletQueryPort;
import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressChainInfo;
import ksh.tryptobackend.wallet.application.port.out.dto.WalletInfo;
import ksh.tryptobackend.wallet.domain.model.DepositAddress;
import ksh.tryptobackend.wallet.domain.vo.DepositTargetExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetDepositAddressService implements GetDepositAddressUseCase {

    private final WalletQueryPort walletQueryPort;
    private final DepositAddressExchangePort exchangePort;
    private final DepositAddressExchangeCoinChainPort chainPort;
    private final DepositAddressPersistencePort depositAddressPersistencePort;

    @Override
    @Transactional
    public DepositAddress getDepositAddress(GetDepositAddressQuery query) {
        WalletInfo wallet = getWallet(query.walletId());
        DepositTargetExchange exchange = exchangePort.getExchange(wallet.exchangeId());
        exchange.validateTransferable(query.coinId());

        DepositAddressChainInfo chainInfo = chainPort.getExchangeCoinChain(
            wallet.exchangeId(), query.coinId(), query.chain());

        return depositAddressPersistencePort.findByWalletIdAndChain(query.walletId(), query.chain())
            .orElseGet(() -> depositAddressPersistencePort.save(
                DepositAddress.create(query.walletId(), query.chain(), chainInfo.tagRequired())));
    }

    private WalletInfo getWallet(Long walletId) {
        return walletQueryPort.findById(walletId)
            .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
    }
}
