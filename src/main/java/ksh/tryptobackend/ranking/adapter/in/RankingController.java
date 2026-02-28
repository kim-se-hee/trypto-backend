package ksh.tryptobackend.ranking.adapter.in;

import jakarta.validation.Valid;
import ksh.tryptobackend.common.dto.response.ApiResponseDto;
import ksh.tryptobackend.common.dto.response.PageResponseDto;
import ksh.tryptobackend.ranking.adapter.in.dto.request.GetRankingsRequest;
import ksh.tryptobackend.ranking.adapter.in.dto.response.RankingItemResponse;
import ksh.tryptobackend.ranking.application.port.in.GetRankingsUseCase;
import ksh.tryptobackend.ranking.application.port.in.dto.result.RankingItemResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final GetRankingsUseCase getRankingsUseCase;

    @GetMapping
    public ApiResponseDto<PageResponseDto<RankingItemResponse>> getRankings(
        @Valid @ModelAttribute GetRankingsRequest request
    ) {
        Page<RankingItemResult> result = getRankingsUseCase.getRankings(request.toQuery());
        Page<RankingItemResponse> responsePage = result.map(RankingItemResponse::from);
        return ApiResponseDto.success("랭킹을 조회했습니다.", PageResponseDto.from(responsePage));
    }
}
