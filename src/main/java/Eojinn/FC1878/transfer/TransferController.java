package Eojinn.FC1878.transfer;

import Eojinn.FC1878.transfer.TransferDtos.TransfersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이적시장: GET /api/transfers?window=2026-summer&league=프리미어리그&korean=true&q=손흥민&sort=fee
 * - window: "2026-summer", "2026-winter", "all" (생략하면 가장 최근 이적시장)
 * - sort: "date"(기본, 최신순) 또는 "fee"(이적료 높은 순)
 */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public TransfersResponse list(@RequestParam(required = false) String window,
                                  @RequestParam(required = false) String league,
                                  @RequestParam(defaultValue = "false") boolean korean,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(defaultValue = "date") String sort) {
        return transferService.search(window, league, korean, q, sort);
    }
}
