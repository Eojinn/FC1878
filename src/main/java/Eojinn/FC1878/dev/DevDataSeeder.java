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
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * football-data.org 토큰(FOOTBALL_API_KEY)이 없을 때, DB가 비어 있으면 화면 확인용 샘플 데이터를 넣는다.
 * 순위·스코어·포인트 등 모든 수치는 가짜 데이터다. 토큰을 넣고 실제 데이터를 받으려면 DB를 비우고 다시 실행한다.
 */
@Slf4j
@Component
@ConditionalOnExpression("'${football.api.key:}'.isBlank()")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    /** football-data.org 대회 엠블럼 */
    private static final String LEAGUE_LOGO = "https://crests.football-data.org/%s.png";
    /** 팀 엠블럼 이미지 (샘플 화면용) */
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
    private final Map<String, Team> teamsByKo = new HashMap<>();
    private final Map<Integer, List<Team>> leagueTeams = new LinkedHashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (leagueRepository.count() > 0) return;
        log.info("[dev] 샘플 데이터를 생성합니다.");

        // 리그 + 팀 (SampleTeams 참고)
        int sampleTeamId = 1_900_000_001; // 실제 football-data.org 팀 ID와 겹치지 않는 임시 externalId
        for (SampleTeams.L l : SampleTeams.LEAGUES) {
            league(l.extId(), l.code(), l.name(), l.ko(), l.country());
            List<Team> teamList = new ArrayList<>();
            for (SampleTeams.T t : l.teams()) {
                teamList.add(team(sampleTeamId++, t.name(), t.ko(), t.logoId()));
            }
            leagueTeams.put(l.extId(), teamList);
        }

        LocalDateTime now = TimeUtil.nowKst().truncatedTo(ChronoUnit.HOURS);
        int id = 1_900_000_001; // 실제 경기 ID와 겹치지 않는 임시 값

        Fixture done = fixture(id++, 2021, "리버풀", "브라이튼", "Regular Season - 13", now.minusDays(1).withHour(23),
                FixtureStatus.FINISHED, 90, 2, 1);
        done.markSettled();
        fixture(id++, 2021, "아스널", "뉴캐슬", "Regular Season - 13", now.minusHours(1), FixtureStatus.LIVE, 67, 1, 1);
        Fixture featured = fixture(id++, 2021, "토트넘", "첼시", "Regular Season - 13", now.plusHours(3),
                FixtureStatus.SCHEDULED, null, null, null);

        // 나머지 샘플 경기: 내일~모레에 나눠 배치
        int[] hours = {21, 23, 2, 4};
        int slot = 0;
        for (var e : SampleTeams.MATCHUPS.entrySet()) {
            for (String[] m : e.getValue()) {
                if (m[0].equals("토트넘")) continue; // 위에서 featured 로 생성
                LocalDateTime kickoff = now.plusDays(1 + slot % 2).withHour(hours[slot % hours.length]);
                fixture(id++, e.getKey(), m[0], m[1], "Regular Season - 13", kickoff,
                        FixtureStatus.SCHEDULED, null, null, null);
                slot++;
            }
        }

        // 리그별 순위표 (가짜 수치: 목록 순서대로 승점이 높게)
        java.util.Random rnd = new java.util.Random(1878);
        String results = "WWWDL";
        for (var e : leagueTeams.entrySet()) {
            List<Team> list = e.getValue();
            if (list.isEmpty()) continue;
            int played = 12;
            int maxPts = played * 3 - played / 2;
            for (int i = 0; i < list.size(); i++) {
                int pts = Math.max(1, maxPts - i * (maxPts / list.size()) - rnd.nextInt(2));
                int win = Math.min(played, pts / 3);
                int draw = Math.min(played - win, pts - win * 3);
                int lose = played - win - draw;
                int gf = win * 2 + draw + rnd.nextInt(5);
                int ga = lose * 2 + draw + rnd.nextInt(5);
                StringBuilder form = new StringBuilder();
                for (int f = 0; f < 5; f++) form.append(results.charAt(rnd.nextInt(results.length())));
                Standing s = Standing.of(leagues.get(e.getKey()), list.get(i), SEASON);
                s.update(i + 1, played, win, draw, lose, gf, ga, win * 3 + draw, form.toString());
                standingRepository.save(s);
            }
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

    private void league(int extId, String code, String name, String ko, String country) {
        League l = League.of(extId, name, country, LEAGUE_LOGO.formatted(code));
        l.changeKoreanName(ko);
        leagues.put(extId, leagueRepository.save(l));
    }

    private Team team(int extId, String name, String ko, int logoId) {
        Team t = Team.of(extId, name, logoId > 0 ? TEAM_LOGO.formatted(logoId) : null);
        t.changeKoreanName(ko);
        Team saved = teamRepository.save(t);
        teamsByKo.put(ko, saved);
        return saved;
    }

    private Fixture fixture(int extId, int league, String homeKo, String awayKo, String round, LocalDateTime kickoff,
                            FixtureStatus status, Integer elapsed, Integer hs, Integer as) {
        Fixture f = Fixture.of(extId, leagues.get(league), teamsByKo.get(homeKo), teamsByKo.get(awayKo), SEASON);
        f.updateState(round, kickoff, status, elapsed, hs, as, null, null);
        return fixtureRepository.save(f);
    }

}
