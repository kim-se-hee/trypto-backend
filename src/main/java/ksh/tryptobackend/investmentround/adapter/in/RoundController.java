package ksh.tryptobackend.investmentround.adapter.in;

import jakarta.validation.Valid;
import ksh.tryptobackend.common.dto.response.ApiResponseDto;
import ksh.tryptobackend.investmentround.adapter.in.dto.request.ChargeEmergencyFundingRequest;
import ksh.tryptobackend.investmentround.adapter.in.dto.request.StartRoundRequest;
import ksh.tryptobackend.investmentround.adapter.in.dto.response.ChargeEmergencyFundingResponse;
import ksh.tryptobackend.investmentround.adapter.in.dto.response.StartRoundResponse;
import ksh.tryptobackend.investmentround.application.port.in.ChargeEmergencyFundingUseCase;
import ksh.tryptobackend.investmentround.application.port.in.StartRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ChargeEmergencyFundingResult;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.StartRoundResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final StartRoundUseCase startRoundUseCase;
    private final ChargeEmergencyFundingUseCase chargeEmergencyFundingUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<StartRoundResponse> createRound(@Valid @RequestBody StartRoundRequest request) {
        StartRoundResult result = startRoundUseCase.startRound(request.toCommand());
        return ApiResponseDto.created("투자 라운드가 시작되었습니다.", StartRoundResponse.from(result));
    }

    @PostMapping("/{roundId}/emergency-funding")
    public ApiResponseDto<ChargeEmergencyFundingResponse> chargeEmergencyFunding(
        @PathVariable Long roundId,
        @Valid @RequestBody ChargeEmergencyFundingRequest request
    ) {
        ChargeEmergencyFundingResult result = chargeEmergencyFundingUseCase
            .chargeEmergencyFunding(request.toCommand(roundId));
        return ApiResponseDto.success("긴급 자금을 투입했습니다.", ChargeEmergencyFundingResponse.from(result));
    }
}
