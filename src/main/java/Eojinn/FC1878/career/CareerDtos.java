package Eojinn.FC1878.career;

import java.util.List;

public final class CareerDtos {

    private CareerDtos() {
    }

    /**
     * 선수 프로필 + 커리어 (출처: Wikidata).
     * found=false 면 Wikidata 에서 선수를 찾지 못한 것 (나머지 값은 비어 있음)
     */
    public record CareerResponse(boolean found, String wikidataId, String sourceUrl, String name, String koreanName,
                                 String birthDate, Integer heightCm, List<String> positions, String nationality,
                                 List<CareerEntry> clubs, List<CareerEntry> nationalTeams,
                                 Integer clubApps, Integer clubGoals) {

        public static CareerResponse notFound() {
            return new CareerResponse(false, null, null, null, null, null, null, List.of(), null,
                    List.of(), List.of(), null, null);
        }
    }

    /** start/end: "2015-08" 또는 "2015" (모르면 null). current: 아직 소속 중(끝 날짜 없음) */
    public record CareerEntry(String team, String teamEn, String start, String end, boolean current, boolean loan,
                              Integer apps, Integer goals) {
    }
}
