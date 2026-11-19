package Eojinn.FC1878.prediction;

import Eojinn.FC1878.prediction.PredictionDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fixtures/{fixtureId}/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    /** 비로그인도 조회 가능 (로그인 시 myPick 포함) */
    @GetMapping
    public PredictionStatsResponse stats(@PathVariable Long fixtureId, @AuthenticationPrincipal Long memberId) {
        return predictionService.stats(fixtureId, memberId);
    }

    @PostMapping
    public PredictionStatsResponse predict(@PathVariable Long fixtureId,
                                           @AuthenticationPrincipal Long memberId,
                                           @Valid @RequestBody PredictRequest req) {
        return predictionService.predict(memberId, fixtureId, req.pick());
    }
}
