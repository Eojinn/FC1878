package Eojinn.FC1878.dev;

import Eojinn.FC1878.community.comment.Comment;
import Eojinn.FC1878.community.comment.CommentRepository;
import Eojinn.FC1878.community.post.Post;
import Eojinn.FC1878.community.post.PostCategory;
import Eojinn.FC1878.community.post.PostRepository;
import Eojinn.FC1878.football.domain.*;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.football.repository.LeagueRepository;
import Eojinn.FC1878.football.repository.StandingRepository;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.common.TimeUtil;
import Eojinn.FC1878.member.Member;
import Eojinn.FC1878.member.MemberRepository;
import Eojinn.FC1878.prediction.Pick;
import Eojinn.FC1878.prediction.Prediction;
import Eojinn.FC1878.prediction.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * dev 프로필에서 DB가 비어 있을 때만 화면 확인용 샘플 데이터를 넣는다.
 * 순위·스코어·포인트 등 모든 수치는 가짜 데이터이며, 외부 API 동기화가 켜지면 실제 데이터로 덮어써진다.
 * 리그/팀 ID 와 엠블럼 URL 은 API-Football 기준.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String LEAGUE_LOGO = "https://media.api-sports.io/football/leagues/%d.png";
    private static final String TEAM_LOGO = "https://media.api-sports.io/football/teams/%d.png";
    private static final int SEASON = 2026;

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final StandingRepository standingRepository;
    private final MemberRepository memberRepository;
    private final PredictionRepository predictionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<Integer, League> leagues = new HashMap<>();
    private final Map<Integer, Team> teams = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (leagueRepository.count() > 0) return;
        log.info("[dev] 샘플 데이터를 생성합니다.");

        league(39, "Premier League", "프리미어리그", "England");
        league(140, "La Liga", "라리가", "Spain");
        league(135, "Serie A", "세리에A", "Italy");
        league(78, "Bundesliga", "분데스리가", "Germany");
        league(61, "Ligue 1", "리그1", "France");
        league(2, "UEFA Champions League", "챔피언스리그", "World");

        team(42, "Arsenal", "아스널");
        team(40, "Liverpool", "리버풀");
        team(50, "Manchester City", "맨시티");
        team(49, "Chelsea", "첼시");
        team(47, "Tottenham", "토트넘");
        team(34, "Newcastle", "뉴캐슬");
        team(51, "Brighton", "브라이튼");
        team(529, "Barcelona", "바르셀로나");
        team(541, "Real Madrid", "레알 마드리드");
        team(533, "Villarreal", "비야레알");
        team(536, "Sevilla", "세비야");
        team(157, "Bayern München", "바이에른 뮌헨");
        team(160, "SC Freiburg", "프라이부르크");
        team(505, "Inter", "인테르");
        team(496, "Juventus", "유벤투스");
        team(85, "Paris Saint Germain", "파리 생제르맹");
        team(81, "Marseille", "마르세유");

        LocalDateTime now = TimeUtil.nowKst().truncatedTo(ChronoUnit.HOURS);
        int id = 900_001; // 실제 API fixture ID와 겹치지 않도록 큰 값 사용

        Fixture done = fixture(id++, 39, 40, 51, "Regular Season - 13", now.minusDays(1).withHour(23),
                FixtureStatus.FINISHED, 90, 2, 1);
        done.markSettled();
        fixture(id++, 39, 42, 34, "Regular Season - 13", now.minusHours(1), FixtureStatus.LIVE, 67, 1, 1);
        Fixture featured = fixture(id++, 39, 47, 49, "Regular Season - 13", now.plusHours(3),
                FixtureStatus.SCHEDULED, null, null, null);
        fixture(id++, 39, 50, 51, "Regular Season - 14", now.plusDays(1).withHour(21),
                FixtureStatus.SCHEDULED, null, null, null);
        fixture(id++, 140, 529, 533, "Regular Season - 14", now.plusDays(1).withHour(22),
                FixtureStatus.SCHEDULED, null, null, null);
        fixture(id++, 78, 157, 160, "Regular Season - 12", now.plusDays(1).withHour(23).withMinute(30),
                FixtureStatus.SCHEDULED, null, null, null);
        fixture(id++, 140, 541, 536, "Regular Season - 14", now.plusDays(2).withHour(4),
                FixtureStatus.SCHEDULED, null, null, null);
        fixture(id++, 135, 505, 496, "Regular Season - 13", now.plusDays(2).withHour(2).withMinute(45),
                FixtureStatus.SCHEDULED, null, null, null);
        fixture(id, 61, 85, 81, "Regular Season - 13", now.plusDays(3).withHour(4).withMinute(45),
                FixtureStatus.SCHEDULED, null, null, null);

        // EPL 순위 (샘플 수치)
        int[][] table = {
                {42, 12, 9, 1, 2, 24, 9}, {40, 12, 8, 2, 2, 25, 12}, {50, 12, 8, 1, 3, 26, 14},
                {49, 12, 6, 4, 2, 21, 13}, {47, 12, 6, 3, 3, 22, 16}, {34, 12, 6, 2, 4, 18, 15},
                {51, 12, 5, 3, 4, 19, 18}};
        String[] forms = {"WWDWW", "WLWWD", "WWLWW", "DWDWL", "WDLWW", "LWWDW", "DWLLW"};
        for (int i = 0; i < table.length; i++) {
            int[] r = table[i];
            Standing s = Standing.of(leagues.get(39), teams.get(r[0]), SEASON);
            s.update(i + 1, r[1], r[2], r[3], r[4], r[5], r[6], r[2] * 3 + r[3], forms[i]);
            standingRepository.save(s);
        }

        // 샘플 회원 (비밀번호: password1234!)
        String pw = passwordEncoder.encode("password1234!");
        String[] nicks = {"골든부트", "토트넘의봄", "빌드업장인", "새벽축구", "역습한방"};
        int[] points = {1240, 1180, 1050, 990, 960};
        Pick[] picks = {Pick.HOME, Pick.HOME, Pick.AWAY, Pick.DRAW, Pick.AWAY};
        Member[] members = new Member[nicks.length];
        for (int i = 0; i < nicks.length; i++) {
            Member m = Member.create("sample" + (i + 1) + "@fc1878.dev", pw, nicks[i]);
            m.addPoint(points[i]);
            members[i] = memberRepository.save(m);
            predictionRepository.save(Prediction.of(members[i], featured, picks[i]));
        }

        // 샘플 게시글
        Object[][] posts = {
                {PostCategory.MATCH, "아스널 후반 교체 타이밍 어떻게 보세요?", 230},
                {PostCategory.TRANSFER, "겨울 이적시장 루머 정리", 410},
                {PostCategory.OVERSEAS, "이번 주말 해외파 선수 출전 일정 모음", 180},
                {PostCategory.FREE, "직관 처음 가는데 좌석 추천 부탁드려요", 95}};
        for (int i = 0; i < posts.length; i++) {
            Post p = Post.create(members[i], (PostCategory) posts[i][0], (String) posts[i][1],
                    "샘플 게시글 본문입니다. 자유롭게 수정하거나 삭제해 보세요.");
            for (int v = 0; v < (int) posts[i][2]; v++) p.increaseView();
            postRepository.save(p);
            commentRepository.save(Comment.create(p, members[(i + 1) % members.length], "좋은 글 감사합니다!"));
        }
        log.info("[dev] 샘플 데이터 생성 완료 (로그인 테스트: sample1@fc1878.dev / password1234!)");
    }

    private void league(int extId, String name, String ko, String country) {
        League l = League.of(extId, name, country, LEAGUE_LOGO.formatted(extId));
        l.changeKoreanName(ko);
        leagues.put(extId, leagueRepository.save(l));
    }

    private void team(int extId, String name, String ko) {
        Team t = Team.of(extId, name, TEAM_LOGO.formatted(extId));
        t.changeKoreanName(ko);
        teams.put(extId, teamRepository.save(t));
    }

    private Fixture fixture(int extId, int league, int home, int away, String round, LocalDateTime kickoff,
                            FixtureStatus status, Integer elapsed, Integer hs, Integer as) {
        Fixture f = Fixture.of(extId, leagues.get(league), teams.get(home), teams.get(away), SEASON);
        f.updateState(round, kickoff, status, elapsed, hs, as, null, null);
        return fixtureRepository.save(f);
    }

}
