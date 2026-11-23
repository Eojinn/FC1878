package Eojinn.FC1878.transfer;

import Eojinn.FC1878.transfer.TransferDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 이적 목록 조회.
 * 직접 관리하는 데이터라 많아야 수백 건이어서, 한 번에 읽고 자바에서 거른다.
 * (수천 건 이상으로 늘면 조건을 DB 쿼리로 옮기면 됨)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    public static final String ALL = "all";

    private final TransferRepository repository;

    public TransfersResponse search(String windowKey, String league, boolean koreanOnly, String keyword, String sort) {
        List<Transfer> all = repository.findAllByOrderByTransferDateDescIdDesc();

        // 이적시장 목록 (최신순) + 건수
        Map<String, Long> counts = all.stream()
                .collect(Collectors.groupingBy(t -> TransferWindow.of(t.getTransferDate()).key(),
                        LinkedHashMap::new, Collectors.counting()));
        List<WindowOption> windows = counts.entrySet().stream()
                .map(e -> new WindowOption(e.getKey(), TransferWindow.parse(e.getKey()).label(), e.getValue()))
                .toList();

        // 이적시장을 안 고르면 가장 최근 이적시장
        TransferWindow window = ALL.equals(windowKey) ? null : TransferWindow.parse(windowKey);
        if (window == null && !ALL.equals(windowKey) && !windows.isEmpty()) {
            window = TransferWindow.parse(windows.get(0).key());
        }
        final TransferWindow selected = window;

        List<Transfer> inWindow = all.stream()
                .filter(t -> selected == null || selected.contains(t.getTransferDate()))
                .toList();

        // 리그 목록: 이 이적시장에서 들어오고 나간 리그, 많은 순
        List<String> leagues = inWindow.stream()
                .flatMap(t -> Stream.of(t.getToLeague(), t.getFromLeague()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .toList();

        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Comparator<Transfer> order = "fee".equals(sort)
                ? Comparator.comparing(Transfer::getFeeMillionEur, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Transfer::getTransferDate, Comparator.reverseOrder())
                : Comparator.comparing(Transfer::getTransferDate, Comparator.reverseOrder())
                        .thenComparing(Transfer::getId, Comparator.reverseOrder());

        List<Transfer> items = inWindow.stream()
                .filter(t -> league == null || league.isBlank()
                        || league.equals(t.getToLeague()) || league.equals(t.getFromLeague()))
                .filter(t -> !koreanOnly || t.isKorean())
                .filter(t -> q.isEmpty() || matches(t, q))
                .sorted(order)
                .toList();

        BigDecimal totalFee = items.stream()
                .map(Transfer::getFeeMillionEur)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Summary summary = new Summary(items.size(), items.stream().filter(Transfer::isKorean).count(), totalFee);

        return new TransfersResponse(selected == null ? ALL : selected.key(), windows, leagues, summary,
                items.stream().map(TransferResponse::from).toList());
    }

    private static boolean matches(Transfer t, String q) {
        return Stream.of(t.getPlayerName(), t.getPlayerNameEn(), t.getFromClub(), t.getToClub())
                .filter(Objects::nonNull)
                .anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(q));
    }
}
