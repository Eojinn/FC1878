package Eojinn.FC1878.career;

import Eojinn.FC1878.career.CareerDtos.CareerResponse;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.player.Player;
import Eojinn.FC1878.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 선수 커리어 (Wikidata)
 * - GET /api/players/{id}/career          : DB에 있는 선수 (영문 이름 + 생년월일로 정확히 찾음)
 * - GET /api/career?name=손흥민&lang=ko&korea=true : 해외파·이적시장처럼 DB에 없는 선수
 */
@RestController
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final PlayerRepository playerRepository;

    @GetMapping("/api/players/{id}/career")
    public CareerResponse playerCareer(@PathVariable Long id) {
        Player p = playerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "선수를 찾을 수 없어요."));
        boolean korea = p.getNationality() != null && p.getNationality().toLowerCase(Locale.ROOT).contains("korea");
        CareerResponse res = careerService.lookup(p.getName(), "en", p.getDateOfBirth(), korea);
        // 커리어에서 한글 이름을 알게 됐으면 저장 → 다음부터 한글로 검색 가능
        if (res.found() && res.koreanName() != null && p.getKoreanName() == null) {
            p.markKoreanName(res.koreanName(), LocalDateTime.now());
            playerRepository.save(p);
        }
        return res;
    }

    @GetMapping("/api/career")
    public CareerResponse career(@RequestParam String name,
                                 @RequestParam(defaultValue = "en") String lang,
                                 @RequestParam(required = false) LocalDate birth,
                                 @RequestParam(defaultValue = "false") boolean korea) {
        if (name.isBlank() || name.length() > 60) throw new BusinessException(ErrorCode.INVALID_INPUT, "이름을 확인해 주세요.");
        return careerService.lookup(name, lang, birth, korea);
    }
}
