package Eojinn.FC1878.football.service;

import java.text.Normalizer;
import java.util.*;

/**
 * 팀 한글 이름·별명 사전 (검색 전용). football-data.org 는 영문 이름만 줘서 "맨유", "토트넘"으로 찾을 수 있게 따로 둠.
 * 화면 표시 이름은 바꾸지 않고, team.search_names 에만 넣어 검색에 씀.
 *
 * 형식: "한글이름, 별명, 별명 = 영문이름, 영문이름 ..." (영문은 football-data 의 짧은 이름·정식 이름을 모두 적음)
 * 새 팀이 안 찾아지면 여기에 한 줄 추가하면 됨 (서버를 다시 켜면 반영).
 */
public final class TeamKoreanNames {

    private static final String[] DATA = {
            // ── 프리미어리그 ──
            "아스널, 아스날 = Arsenal, Arsenal FC",
            "애스턴 빌라, 아스톤 빌라, 빌라 = Aston Villa, Aston Villa FC",
            "본머스 = Bournemouth, AFC Bournemouth",
            "브렌트퍼드, 브렌트포드 = Brentford, Brentford FC",
            "브라이턴, 브라이튼 = Brighton Hove, Brighton & Hove Albion FC, Brighton",
            "번리 = Burnley, Burnley FC",
            "첼시 = Chelsea, Chelsea FC",
            "크리스털 팰리스, 크리스탈 팰리스, 팰리스 = Crystal Palace, Crystal Palace FC",
            "에버턴, 에버튼 = Everton, Everton FC",
            "풀럼 = Fulham, Fulham FC",
            "입스위치 = Ipswich Town, Ipswich Town FC, Ipswich",
            "리즈 유나이티드, 리즈 = Leeds United, Leeds United FC, Leeds",
            "레스터 시티, 레스터 = Leicester City, Leicester City FC, Leicester",
            "리버풀 = Liverpool, Liverpool FC",
            "맨체스터 시티, 맨시티, 맨체스터시티 = Man City, Manchester City FC, Manchester City",
            "맨체스터 유나이티드, 맨유, 맨체스터유나이티드 = Man United, Manchester United FC, Manchester United",
            "뉴캐슬 유나이티드, 뉴캐슬 = Newcastle, Newcastle United FC, Newcastle United",
            "노팅엄 포리스트, 노팅엄, 노팅햄 = Nottingham, Nottingham Forest FC, Nottingham Forest",
            "사우샘프턴, 사우스햄튼 = Southampton, Southampton FC",
            "선덜랜드 = Sunderland, Sunderland AFC",
            "토트넘 홋스퍼, 토트넘 = Tottenham, Tottenham Hotspur FC, Tottenham Hotspur, Spurs",
            "웨스트햄 유나이티드, 웨스트햄 = West Ham, West Ham United FC, West Ham United",
            "울버햄튼, 울브스, 울버햄프턴 = Wolverhampton, Wolverhampton Wanderers FC, Wolves",
            "셰필드 유나이티드 = Sheffield United, Sheffield United FC",
            "루턴 타운 = Luton Town, Luton Town FC",
            // ── 라리가 ──
            "바르셀로나, 바르사, 바르샤 = Barça, FC Barcelona, Barcelona",
            "레알 마드리드, 레알, 레알마드리드 = Real Madrid, Real Madrid CF",
            "아틀레티코 마드리드, 아틀레티코, 아틀레티 = Atleti, Club Atlético de Madrid, Atletico Madrid",
            "아틀레틱 빌바오, 빌바오 = Athletic, Athletic Club",
            "세비야 = Sevilla FC, Sevilla",
            "레알 베티스, 베티스 = Real Betis, Real Betis Balompié",
            "레알 소시에다드, 소시에다드 = Real Sociedad, Real Sociedad de Fútbol",
            "비야레알 = Villarreal, Villarreal CF",
            "발렌시아 = Valencia, Valencia CF",
            "헤타페 = Getafe, Getafe CF",
            "오사수나 = Osasuna, CA Osasuna",
            "셀타 비고, 셀타 = Celta, RC Celta de Vigo, Celta Vigo",
            "마요르카 = Mallorca, RCD Mallorca",
            "라요 바예카노, 라요 = Rayo Vallecano, Rayo Vallecano de Madrid",
            "지로나 = Girona, Girona FC",
            "알라베스 = Alavés, Deportivo Alavés",
            "에스파뇰 = Espanyol, RCD Espanyol de Barcelona",
            "레가네스 = Leganés, CD Leganés",
            "라스팔마스 = Las Palmas, UD Las Palmas",
            "바야돌리드 = Valladolid, Real Valladolid CF",
            "레반테 = Levante, Levante UD",
            "엘체 = Elche, Elche CF",
            "오비에도 = Oviedo, Real Oviedo",
            // ── 세리에 A ──
            "인터 밀란, 인테르, 인터 = Inter, FC Internazionale Milano, Inter Milan",
            "AC 밀란, 밀란 = Milan, AC Milan",
            "유벤투스, 유베 = Juventus, Juventus FC",
            "나폴리 = Napoli, SSC Napoli",
            "AS 로마, 로마 = Roma, AS Roma",
            "라치오 = Lazio, SS Lazio",
            "아탈란타 = Atalanta, Atalanta BC",
            "피오렌티나 = Fiorentina, ACF Fiorentina",
            "볼로냐 = Bologna, Bologna FC 1909",
            "토리노 = Torino, Torino FC",
            "제노아 = Genoa, Genoa CFC",
            "우디네세 = Udinese, Udinese Calcio",
            "레체 = Lecce, US Lecce",
            "칼리아리 = Cagliari, Cagliari Calcio",
            "베로나 = Verona, Hellas Verona FC",
            "엠폴리 = Empoli, Empoli FC",
            "코모 = Como 1907, Como",
            "파르마 = Parma, Parma Calcio 1913",
            "몬차 = Monza, AC Monza",
            "베네치아 = Venezia, Venezia FC",
            "사수올로 = Sassuolo, US Sassuolo Calcio",
            "피사 = Pisa, AC Pisa 1909",
            "크레모네세 = Cremonese, US Cremonese",
            // ── 분데스리가 ──
            "바이에른 뮌헨, 뮌헨, 바이에른 = Bayern, FC Bayern München, Bayern Munich",
            "보루시아 도르트문트, 도르트문트, 돌문 = Dortmund, Borussia Dortmund",
            "바이어 레버쿠젠, 레버쿠젠 = Leverkusen, Bayer 04 Leverkusen",
            "RB 라이프치히, 라이프치히 = RB Leipzig",
            "아인트라흐트 프랑크푸르트, 프랑크푸르트 = Frankfurt, Eintracht Frankfurt",
            "슈투트가르트 = Stuttgart, VfB Stuttgart",
            "프라이부르크 = Freiburg, SC Freiburg",
            "볼프스부르크 = Wolfsburg, VfL Wolfsburg",
            "묀헨글라트바흐, 글라트바흐 = M'gladbach, Borussia Mönchengladbach",
            "우니온 베를린 = Union Berlin, 1. FC Union Berlin",
            "베르더 브레멘, 브레멘 = Werder Bremen, SV Werder Bremen, Werder",
            "마인츠 = Mainz, 1. FSV Mainz 05",
            "호펜하임 = Hoffenheim, TSG 1899 Hoffenheim",
            "아우크스부르크 = Augsburg, FC Augsburg",
            "하이덴하임 = Heidenheim, 1. FC Heidenheim 1846",
            "보훔 = Bochum, VfL Bochum 1848",
            "장크트 파울리, 상파울리 = St. Pauli, FC St. Pauli 1910",
            "홀슈타인 킬, 킬 = Holstein Kiel",
            "쾰른 = Köln, 1. FC Köln",
            "함부르크 = HSV, Hamburger SV",
            // ── 리그 1 ──
            "파리 생제르맹, 파리, PSG, 파리생제르맹 = PSG, Paris Saint-Germain FC",
            "마르세유 = Marseille, Olympique de Marseille",
            "모나코 = Monaco, AS Monaco FC",
            "릴 = Lille, Lille OSC",
            "리옹 = Lyon, Olympique Lyonnais",
            "니스 = Nice, OGC Nice",
            "랑스 = Lens, Racing Club de Lens, RC Lens",
            "렌 = Rennes, Stade Rennais FC 1901",
            "브레스트 = Brest, Stade Brestois 29",
            "스트라스부르 = Strasbourg, RC Strasbourg Alsace",
            "툴루즈 = Toulouse, Toulouse FC",
            "몽펠리에 = Montpellier, Montpellier HSC",
            "낭트 = Nantes, FC Nantes",
            "랭스 = Reims, Stade de Reims",
            "오세르 = Auxerre, AJ Auxerre",
            "앙제 = Angers, Angers SCO",
            "르아브르 = Le Havre, Le Havre AC",
            "생테티엔 = Saint-Étienne, AS Saint-Étienne",
            "로리앙 = Lorient, FC Lorient",
            "메스 = Metz, FC Metz",
            "파리 FC = Paris FC",
            // ── 챔피언스리그 단골 (다른 리그) ──
            "벤피카 = Benfica, SL Benfica",
            "포르투 = Porto, FC Porto",
            "스포르팅 = Sporting CP, Sporting Clube de Portugal",
            "브라가 = Braga, Sporting Clube de Braga",
            "아약스 = Ajax, AFC Ajax",
            "PSV 에인트호번, 에인트호번 = PSV, PSV Eindhoven",
            "페예노르트 = Feyenoord, Feyenoord Rotterdam",
            "셀틱 = Celtic, Celtic FC",
            "레인저스 = Rangers, Rangers FC",
            "클뢰브 브뤼허, 브뤼헤 = Club Brugge, Club Brugge KV",
            "갈라타사라이 = Galatasaray, Galatasaray SK",
            "페네르바흐체 = Fenerbahçe, Fenerbahçe SK",
            "잘츠부르크 = Salzburg, FC Red Bull Salzburg, RB Salzburg",
            "샤흐타르 도네츠크, 샤흐타르 = Shakhtar, FC Shakhtar Donetsk",
            "코펜하겐 = Copenhagen, FC København",
            "올림피아코스 = Olympiacos, Olympiacos FC",
            "슬라비아 프라하 = Slavia Praha, SK Slavia Praha",
            "츠르베나 즈베즈다 = Crvena Zvezda, FK Crvena Zvezda",
            "디나모 자그레브 = Dinamo Zagreb, GNK Dinamo Zagreb",
            "영 보이스 = Young Boys, BSC Young Boys",
            "슈투름 그라츠 = Sturm Graz, SK Sturm Graz",
            "보되/글림트 = Bodø/Glimt, FK Bodø/Glimt",
            "우니온 생질루아즈 = Union SG, Royale Union Saint-Gilloise",
            "카라바흐 = Qarabağ, Qarabağ Ağdam FK",
            "파포스 = Pafos, Pafos FC",
            "카이라트 = Kairat, FC Kairat",
    };

    /** 정규화한 영문 이름 → "한글이름 별명 ..." (검색용, 공백으로 구분) */
    private static final Map<String, String> BY_ENGLISH = new HashMap<>();
    private static final Set<String> NOISE = Set.of("fc", "cf", "afc", "ac", "sc", "cfc", "bc", "hsc", "osc", "kv", "sk", "fk");

    static {
        for (String line : DATA) {
            String[] parts = line.split("=", 2);
            List<String> korean = Arrays.stream(parts[0].split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            String names = String.join(" | ", korean);
            for (String en : parts[1].split(",")) {
                String key = key(en);
                if (!key.isEmpty()) BY_ENGLISH.putIfAbsent(key, names);
            }
        }
    }

    private TeamKoreanNames() {
    }

    /** 영문 팀 이름에 맞는 한글 이름·별명 ("맨체스터 유나이티드 | 맨유 | …"). 사전에 없으면 null */
    public static String namesFor(String englishName) {
        return englishName == null ? null : BY_ENGLISH.get(key(englishName));
    }

    /** 첫 번째 한글 이름 (검색 결과 보조 표시용) */
    public static String primaryKorean(String searchNames) {
        if (searchNames == null || searchNames.isBlank()) return null;
        int bar = searchNames.indexOf(" | ");
        return bar < 0 ? searchNames : searchNames.substring(0, bar);
    }

    /** 악센트·기호·FC 같은 꼬리표를 떼고 비교 ("FC Bayern München" → "bayern munchen") */
    static String key(String name) {
        String n = Normalizer.normalize(name.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replace("ß", "ss").replace("ø", "o").replaceAll("[^a-z0-9]+", " ").trim();
        return Arrays.stream(n.split(" ")).filter(t -> !t.isEmpty() && !NOISE.contains(t)).reduce((a, b) -> a + " " + b).orElse("");
    }
}
