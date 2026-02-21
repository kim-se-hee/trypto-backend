package ksh.tryptobackend.trading.adapter.in;

import jakarta.validation.Valid;
import ksh.tryptobackend.common.dto.response.ApiResponseDto;
import ksh.tryptobackend.trading.adapter.in.dto.request.PlaceOrderRequest;
import ksh.tryptobackend.trading.adapter.in.dto.response.PlaceOrderResponse;
import ksh.tryptobackend.trading.application.port.in.PlaceOrderUseCase;
import ksh.tryptobackend.trading.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<PlaceOrderResponse> createOrder(@Valid @RequestBody PlaceOrderRequest request) {
        Order order = placeOrderUseCase.placeOrder(request.toCommand());
        return ApiResponseDto.created("주문이 처리되었습니다.", PlaceOrderResponse.from(order));
    }
}