package Eojinn.FC1878.team;

import Eojinn.FC1878.team.ProfileDtos.PlayerProfileResponse;
import Eojinn.FC1878.team.ProfileDtos.TeamProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 팀 페이지: GET /api/teams/{id}/profile  (정보·순위·최근/다음 경기·선수단과 기록)
 * 선수 페이지: GET /api/players/{id}       (프로필·대회별 기록·최근 이적)
 */
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/api/teams/{id}/profile")
    public TeamProfileResponse team(@PathVariable Long id) {
        return profileService.team(id);
    }

    @GetMapping("/api/players/{id}")
    public PlayerProfileResponse player(@PathVariable Long id) {
        return profileService.player(id);
    }
}
