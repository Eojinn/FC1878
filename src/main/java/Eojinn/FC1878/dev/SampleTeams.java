package Eojinn.FC1878.dev;

import java.util.List;
import java.util.Map;

/**
 * dev 샘플 데이터용 리그별 참가 팀 (2026-27 시즌, K리그1은 2026 시즌 기준).
 * extId 는 API-Football 팀 ID 이고, 확실하지 않은 팀은 0 으로 두어 엠블럼 없이(약자 원) 표시한다.
 * 실제 API 동기화를 켜면 진짜 ID·이름·엠블럼으로 채워진다.
 */
final class SampleTeams {

    record T(int extId, String name, String ko) {
    }

    record L(int extId, String name, String ko, String country, List<T> teams) {
    }

    private SampleTeams() {
    }

    static final List<L> LEAGUES = List.of(
            new L(39, "Premier League", "프리미어리그", "England", List.of(
                    new T(42, "Arsenal", "아스널"), new T(40, "Liverpool", "리버풀"),
                    new T(50, "Manchester City", "맨시티"), new T(49, "Chelsea", "첼시"),
                    new T(33, "Manchester United", "맨유"), new T(47, "Tottenham", "토트넘"),
                    new T(34, "Newcastle", "뉴캐슬"), new T(66, "Aston Villa", "아스톤 빌라"),
                    new T(51, "Brighton", "브라이튼"), new T(65, "Nottingham Forest", "노팅엄 포레스트"),
                    new T(55, "Brentford", "브렌트퍼드"), new T(35, "Bournemouth", "본머스"),
                    new T(52, "Crystal Palace", "크리스탈 팰리스"), new T(36, "Fulham", "풀럼"),
                    new T(45, "Everton", "에버턴"), new T(746, "Sunderland", "선덜랜드"),
                    new T(63, "Leeds", "리즈"), new T(0, "Coventry", "코번트리"),
                    new T(57, "Ipswich", "입스위치"), new T(64, "Hull City", "헐 시티"))),
            new L(140, "La Liga", "라리가", "Spain", List.of(
                    new T(529, "Barcelona", "바르셀로나"), new T(541, "Real Madrid", "레알 마드리드"),
                    new T(530, "Atletico Madrid", "아틀레티코 마드리드"), new T(531, "Athletic Club", "아틀레틱 빌바오"),
                    new T(533, "Villarreal", "비야레알"), new T(543, "Real Betis", "레알 베티스"),
                    new T(548, "Real Sociedad", "레알 소시에다드"), new T(538, "Celta Vigo", "셀타 비고"),
                    new T(536, "Sevilla", "세비야"), new T(532, "Valencia", "발렌시아"),
                    new T(727, "Osasuna", "오사수나"), new T(728, "Rayo Vallecano", "라요 바예카노"),
                    new T(546, "Getafe", "헤타페"), new T(540, "Espanyol", "에스파뇰"),
                    new T(542, "Alaves", "알라베스"), new T(539, "Levante", "레반테"),
                    new T(797, "Elche", "엘체"), new T(0, "Racing Santander", "라싱 산탄데르"),
                    new T(0, "Deportivo La Coruna", "데포르티보"), new T(0, "Malaga", "말라가"))),
            new L(135, "Serie A", "세리에A", "Italy", List.of(
                    new T(505, "Inter", "인테르"), new T(492, "Napoli", "나폴리"),
                    new T(497, "AS Roma", "AS 로마"), new T(895, "Como", "코모"),
                    new T(489, "AC Milan", "AC 밀란"), new T(496, "Juventus", "유벤투스"),
                    new T(499, "Atalanta", "아탈란타"), new T(487, "Lazio", "라치오"),
                    new T(500, "Bologna", "볼로냐"), new T(502, "Fiorentina", "피오렌티나"),
                    new T(503, "Torino", "토리노"), new T(494, "Udinese", "우디네세"),
                    new T(495, "Genoa", "제노아"), new T(523, "Parma", "파르마"),
                    new T(490, "Cagliari", "칼리아리"), new T(488, "Sassuolo", "사수올로"),
                    new T(867, "Lecce", "레체"), new T(517, "Venezia", "베네치아"),
                    new T(512, "Frosinone", "프로시노네"), new T(1579, "Monza", "몬차"))),
            new L(78, "Bundesliga", "분데스리가", "Germany", List.of(
                    new T(157, "Bayern München", "바이에른 뮌헨"), new T(165, "Borussia Dortmund", "도르트문트"),
                    new T(173, "RB Leipzig", "라이프치히"), new T(168, "Bayer Leverkusen", "레버쿠젠"),
                    new T(172, "VfB Stuttgart", "슈투트가르트"), new T(169, "Eintracht Frankfurt", "프랑크푸르트"),
                    new T(160, "SC Freiburg", "프라이부르크"), new T(167, "Hoffenheim", "호펜하임"),
                    new T(164, "FSV Mainz 05", "마인츠"), new T(182, "Union Berlin", "우니온 베를린"),
                    new T(162, "Werder Bremen", "베르더 브레멘"), new T(163, "Borussia Mönchengladbach", "묀헨글라트바흐"),
                    new T(170, "FC Augsburg", "아우크스부르크"), new T(192, "1. FC Köln", "쾰른"),
                    new T(175, "Hamburger SV", "함부르크"), new T(174, "Schalke 04", "샬케"),
                    new T(0, "SV Elversberg", "엘버스베르크"), new T(0, "SC Paderborn", "파더보른"))),
            new L(61, "Ligue 1", "리그1", "France", List.of(
                    new T(85, "Paris Saint Germain", "파리 생제르맹"), new T(116, "Lens", "랑스"),
                    new T(79, "Lille", "릴"), new T(80, "Lyon", "리옹"),
                    new T(81, "Marseille", "마르세유"), new T(94, "Rennes", "렌"),
                    new T(91, "Monaco", "모나코"), new T(95, "Strasbourg", "스트라스부르"),
                    new T(84, "Nice", "니스"), new T(96, "Toulouse", "툴루즈"),
                    new T(106, "Stade Brestois 29", "브레스트"), new T(108, "Auxerre", "오세르"),
                    new T(77, "Angers", "앙제"), new T(111, "Le Havre", "르아브르"),
                    new T(97, "Lorient", "로리앙"), new T(114, "Paris FC", "파리 FC"),
                    new T(110, "Troyes", "트루아"), new T(0, "Le Mans", "르망"))),
            new L(292, "K League 1", "K리그1", "South-Korea", List.of(
                    new T(0, "Jeonbuk Motors", "전북 현대"), new T(0, "Daejeon Hana Citizen", "대전하나시티즌"),
                    new T(0, "Gimcheon Sangmu", "김천 상무"), new T(0, "Pohang Steelers", "포항 스틸러스"),
                    new T(0, "FC Seoul", "FC 서울"), new T(0, "Gangwon FC", "강원 FC"),
                    new T(0, "Gwangju FC", "광주 FC"), new T(0, "FC Anyang", "FC 안양"),
                    new T(0, "Ulsan HD", "울산 HD"), new T(0, "Jeju SK", "제주 SK"),
                    new T(0, "Incheon United", "인천 유나이티드"), new T(0, "Bucheon FC 1995", "부천 FC"))),
            new L(2, "UEFA Champions League", "챔피언스리그", "World", List.of())
    );

    /** 샘플 경기: {리그 extId, 홈 한글명, 원정 한글명} */
    static final Map<Integer, List<String[]>> MATCHUPS = Map.of(
            39, List.of(new String[]{"토트넘", "첼시"}, new String[]{"맨유", "아스톤 빌라"}, new String[]{"맨시티", "브라이튼"}),
            140, List.of(new String[]{"바르셀로나", "비야레알"}, new String[]{"레알 마드리드", "세비야"}),
            135, List.of(new String[]{"인테르", "유벤투스"}, new String[]{"AS 로마", "나폴리"}),
            78, List.of(new String[]{"바이에른 뮌헨", "프라이부르크"}, new String[]{"도르트문트", "샬케"}),
            61, List.of(new String[]{"파리 생제르맹", "마르세유"}, new String[]{"랑스", "릴"}),
            292, List.of(new String[]{"전북 현대", "대전하나시티즌"}, new String[]{"FC 서울", "울산 HD"})
    );
}
