package Eojinn.FC1878.tactic;

import Eojinn.FC1878.tactic.SquadService.SquadPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 전술판용 팀 스쿼드: GET /api/teams/{id}/squad (로그인 없이 조회 가능) */
@RestController
@RequiredArgsConstructor
public class SquadController {

    private final SquadService squadService;

    @GetMapping("/api/teams/{id}/squad")
    public List<SquadPlayerResponse> squad(@PathVariable Long id) {
        return squadService.squad(id);
    }
}
