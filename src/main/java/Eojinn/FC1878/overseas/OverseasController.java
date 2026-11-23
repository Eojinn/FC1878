package Eojinn.FC1878.overseas;

import Eojinn.FC1878.overseas.OverseasDtos.OverseasResponse;
import Eojinn.FC1878.overseas.OverseasDtos.PlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** 해외파 목록: GET /api/overseas */
@RestController
@RequestMapping("/api/overseas")
@RequiredArgsConstructor
public class OverseasController {

    private final OverseasPlayerRepository repository;

    @GetMapping
    @Transactional(readOnly = true)
    public OverseasResponse list() {
        List<OverseasPlayer> players = repository.findAllByOrderBySortOrderAscIdAsc();
        LocalDate asOf = players.stream()
                .map(p -> p.getUpdatedAt().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);
        return new OverseasResponse(asOf, players.stream().map(PlayerResponse::from).toList());
    }
}
