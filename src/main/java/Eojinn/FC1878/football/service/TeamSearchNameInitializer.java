package Eojinn.FC1878.football.service;

import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 서버가 켜질 때 이미 DB 에 있는 팀들에 검색용 한글 이름을 채움.
 * (새로 들어오는 팀은 FootballSyncService 가 동기화할 때 채움)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamSearchNameInitializer {

    private final TeamRepository teamRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fill() {
        int updated = 0;
        for (Team t : teamRepository.findAll()) {
            String names = TeamKoreanNames.namesFor(t.getName());
            if (names == null && t.getKoreanName() != null) names = t.getKoreanName();
            if (names != null && !Objects.equals(names, t.getSearchNames())) {
                t.changeSearchNames(names);
                updated++;
            }
        }
        if (updated > 0) log.info("팀 검색용 한글 이름 {}개 채움", updated);
    }
}
