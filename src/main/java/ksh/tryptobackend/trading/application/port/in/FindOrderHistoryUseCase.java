package ksh.tryptobackend.trading.application.port.in;

import ksh.tryptobackend.common.dto.response.CursorPageResponseDto;
import ksh.tryptobackend.trading.adapter.in.dto.query.FindOrderHistoryQuery;
import ksh.tryptobackend.trading.adapter.in.dto.response.OrderHistoryResponse;

public interface FindOrderHistoryUseCase {

    CursorPageResponseDto<OrderHistoryResponse> findOrderHistory(FindOrderHistoryQuery query);
}
