package ksh.tryptobackend.transfer.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindWithdrawalFeeUseCase;
import ksh.tryptobackend.transfer.application.port.out.TransferWithdrawalFeeQueryPort;
import ksh.tryptobackend.transfer.domain.vo.WithdrawalCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferWithdrawalFeeQueryAdapter implements TransferWithdrawalFeeQueryPort {

    private final FindWithdrawalFeeUseCase findWithdrawalFeeUseCase;

    @Override
    public WithdrawalCondition getWithdrawalFee(Long exchangeId, Long coinId, String chain) {
        return findWithdrawalFeeUseCase.findByExchangeIdAndCoinIdAndChain(exchangeId, coinId, chain)
            .map(result -> new WithdrawalCondition(result.fee(), result.minWithdrawal()))
            .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_CHAIN));
    }
}
