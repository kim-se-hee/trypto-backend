package ksh.tryptobackend.wallet.application.port.out.dto;

public record DepositAddressInfo(
    Long depositAddressId,
    Long walletId,
    String chain,
    String address,
    String tag
) {
}
