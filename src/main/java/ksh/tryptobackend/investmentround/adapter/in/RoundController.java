package ksh.tryptobackend.investmentround.adapter.in;

import jakarta.validation.Valid;
import ksh.tryptobackend.common.dto.response.ApiResponseDto;
import ksh.tryptobackend.investmentround.adapter.in.dto.request.ChargeEmergencyFundingRequest;
import ksh.tryptobackend.investmentround.adapter.in.dto.request.EndRoundRequest;
import ksh.tryptobackend.investmentround.adapter.in.dto.request.GetActiveRoundRequest;
import ksh.tryptobackend.investmentround.adapter.in.dto.request.StartRoundRequest;
import ksh.tryptobackend.investmentround.adapter.in.dto.response.ChargeEmergencyFundingResponse;
import ksh.tryptobackend.investmentround.adapter.in.dto.response.EndRoundResponse;
import ksh.tryptobackend.investmentround.adapter.in.dto.response.GetActiveRoundResponse;
import ksh.tryptobackend.investmentround.adapter.in.dto.response.StartRoundResponse;
import ksh.tryptobackend.investmentround.application.port.in.ChargeEmergencyFundingUseCase;
import ksh.tryptobackend.investmentround.application.port.in.EndRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.GetActiveRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.StartRoundUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ChargeEmergencyFundingResult;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.EndRoundResult;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.GetActiveRoundResult;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.StartRoundResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final GetActiveRoundUseCase getActiveRoundUseCase;
    private final EndRoundUseCase endRoundUseCase;
    private final ChargeEmergencyFundingUseCase chargeEmergencyFundingUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<StartRoundResponse> createRound(@Valid @RequestBody StartRoundRequest request) {
        StartRoundResult result = startRoundUseCase.startRound(request.toCommand());
        return ApiResponseDto.created("Round started.", StartRoundResponse.from(result));
    }

    @GetMapping("/active")
    public ApiResponseDto<GetActiveRoundResponse> getActiveRound(@Valid @ModelAttribute GetActiveRoundRequest request) {
        GetActiveRoundResult result = getActiveRoundUseCase.getActiveRound(request.toQuery());
        return ApiResponseDto.success("Active round retrieved.", GetActiveRoundResponse.from(result));
    }

    @PostMapping("/{roundId}/end")
    public ApiResponseDto<EndRoundResponse> endRound(
        @PathVariable Long roundId,
        @Valid @RequestBody EndRoundRequest request
    ) {
        EndRoundResult result = endRoundUseCase.endRound(request.toCommand(roundId));
        return ApiResponseDto.success("Round ended.", EndRoundResponse.from(result));
    }

    @PostMapping("/{roundId}/emergency-funding")
    public ApiResponseDto<ChargeEmergencyFundingResponse> chargeEmergencyFunding(
        @PathVariable Long roundId,
        @Valid @RequestBody ChargeEmergencyFundingRequest request
    ) {
        ChargeEmergencyFundingResult result = chargeEmergencyFundingUseCase.chargeEmergencyFunding(
            request.toCommand(roundId)
        );
        return ApiResponseDto.success("Emergency funding charged.", ChargeEmergencyFundingResponse.from(result));
    }
}
