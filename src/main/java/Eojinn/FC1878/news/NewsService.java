package Eojinn.FC1878.news;

import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.news.NewsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 해외축구 뉴스.
 * - 같은 검색은 몇 분 동안 서버 메모리에 저장해 두고 씀 (네이버 하루 호출 한도 아끼기)
 * - 네이버 이용 조건에 맞게 제목·요약은 손대지 않고(태그만 지움), 출처와 원문 링크를 함께 보여줌
 */
@Service
@RequiredArgsConstructor
public class NewsService {

    public static final int PAGE_SIZE = 20;
    private static final int MAX_START = 1000; // 네이버 start 최대값
    private static final int MAX_CACHE = 300;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Pattern TAG = Pattern.compile("<[^>]+>");
    private static final Pattern NUM_ENTITY = Pattern.compile("&#(x?[0-9a-fA-F]+);");

    private final NaverNewsClient client;
    private final NewsProperties props;
    private final Map<String, Cached> cache = new LinkedHashMap<>();

    public NewsPageResponse search(String topicKey, String keyword, int start) {
        NewsTopic topic = NewsTopic.from(topicKey);
        String q = keyword == null ? "" : keyword.trim();
        if (q.length() > 40) throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어는 40자까지 입력할 수 있어요.");
        String query = q.isEmpty() ? topic.query() : (q.contains("축구") ? q : q + " 축구");
        int from = Math.max(1, Math.min(start, MAX_START));
        List<TopicOption> topics = Arrays.stream(NewsTopic.values())
                .map(t -> new TopicOption(t.name(), t.label())).toList();

        if (!props.configured()) {
            return new NewsPageResponse(topic.name(), query, topics, SampleNews.items(query, from), from, from == 1, true);
        }

        String key = query + "|" + from;
        Cached hit = get(key);
        NaverNewsResponse res = hit != null ? hit.response() : client.search(query, from, PAGE_SIZE);
        if (res == null) throw new BusinessException(ErrorCode.NEWS_UNAVAILABLE);
        if (hit == null) put(key, res);

        List<NewsItem> items = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        for (NaverNewsItem it : Optional.ofNullable(res.items()).orElse(List.of())) {
            String title = clean(it.title());
            if (title.isEmpty() || !seenTitles.add(title)) continue; // 같은 제목은 한 번만
            String original = blankToNull(it.originallink());
            String link = Optional.ofNullable(blankToNull(it.link())).orElse(original);
            if (link == null) continue;
            items.add(new NewsItem(title, clean(it.description()), link, original,
                    sourceOf(original != null ? original : link), parseDate(it.pubDate())));
        }
        int total = res.total() == null ? 0 : res.total();
        boolean hasMore = from + PAGE_SIZE <= Math.min(total, MAX_START);
        return new NewsPageResponse(topic.name(), query, topics, items, from, hasMore, false);
    }

    /* ===== 캐시 (오래된 것부터 지움) ===== */

    private synchronized Cached get(String key) {
        Cached c = cache.get(key);
        if (c == null || c.expiresAt().isBefore(Instant.now())) return null;
        return c;
    }

    private synchronized void put(String key, NaverNewsResponse res) {
        if (cache.size() >= MAX_CACHE) cache.remove(cache.keySet().iterator().next());
        cache.put(key, new Cached(res, Instant.now().plus(Duration.ofMinutes(props.cacheMinutes()))));
    }

    private record Cached(NaverNewsResponse response, Instant expiresAt) {
    }

    /* ===== 표시용 정리 ===== */

    /** <b> 태그를 지우고 &quot; 같은 HTML 문자를 원래 글자로 */
    static String clean(String s) {
        if (s == null) return "";
        String t = TAG.matcher(s).replaceAll("");
        t = t.replace("&quot;", "\"").replace("&apos;", "'").replace("&#39;", "'")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ");
        Matcher m = NUM_ENTITY.matcher(t);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String n = m.group(1);
            int code = n.startsWith("x") ? Integer.parseInt(n.substring(1), 16) : Integer.parseInt(n);
            m.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(code))));
        }
        m.appendTail(sb);
        return sb.toString().replace("&amp;", "&").trim();
    }

    /** "Tue, 24 Nov 2026 15:00:00 +0900" → 한국 시간 */
    static LocalDateTime parseDate(String pubDate) {
        try {
            return ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(KST).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    /** 원문 주소로 언론사 이름 (모르면 도메인) */
    static String sourceOf(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) return null;
            host = host.toLowerCase(Locale.ROOT).replaceFirst("^(www|m|mobile)\\.", "");
            for (Map.Entry<String, String> e : SOURCES.entrySet()) {
                if (host.equals(e.getKey()) || host.endsWith("." + e.getKey())) return e.getValue();
            }
            return host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /** 자주 나오는 언론사 (긴 도메인이 먼저 맞도록 순서 유지) */
    private static final Map<String, String> SOURCES = new LinkedHashMap<>();

    static {
        String[][] list = {
                {"n.news.naver.com", "네이버 뉴스"}, {"sports.news.naver.com", "네이버 스포츠"},
                {"sports.khan.co.kr", "스포츠경향"}, {"sports.chosun.com", "스포츠조선"}, {"sports.donga.com", "스포츠동아"},
                {"sports.hankooki.com", "스포츠한국"}, {"osen.co.kr", "OSEN"}, {"xportsnews.com", "엑스포츠뉴스"},
                {"starnewskorea.com", "스타뉴스"}, {"sportsseoul.com", "스포츠서울"}, {"isplus.com", "일간스포츠"},
                {"sportsworldi.com", "스포츠월드"}, {"mydaily.co.kr", "마이데일리"}, {"spotvnews.co.kr", "스포티비뉴스"},
                {"sportalkorea.com", "스포탈코리아"}, {"interfootball.co.kr", "인터풋볼"}, {"footballist.co.kr", "풋볼리스트"},
                {"besteleven.com", "베스트일레븐"}, {"goal.com", "골닷컴"}, {"news1.kr", "뉴스1"}, {"yna.co.kr", "연합뉴스"},
                {"newsis.com", "뉴시스"}, {"mt.co.kr", "머니투데이"}, {"mk.co.kr", "매일경제"}, {"hankyung.com", "한국경제"},
                {"chosun.com", "조선일보"}, {"joongang.co.kr", "중앙일보"}, {"donga.com", "동아일보"}, {"hani.co.kr", "한겨레"},
                {"khan.co.kr", "경향신문"}, {"seoul.co.kr", "서울신문"}, {"segye.com", "세계일보"}, {"kmib.co.kr", "국민일보"},
                {"ytn.co.kr", "YTN"}, {"kbs.co.kr", "KBS"}, {"imbc.com", "MBC"}, {"sbs.co.kr", "SBS"}, {"jtbc.co.kr", "JTBC"},
                {"wikitree.co.kr", "위키트리"}, {"insight.co.kr", "인사이트"}, {"daum.net", "다음"},
        };
        for (String[] s : list) SOURCES.put(s[0], s[1]);
    }
}
