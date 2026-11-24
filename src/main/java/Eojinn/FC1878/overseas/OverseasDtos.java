package Eojinn.FC1878.overseas;

import java.time.LocalDate;
import java.util.List;

public final class OverseasDtos {

    private OverseasDtos() {
    }

    /** asOf: 목록이 마지막으로 바뀐 날짜 (화면에 "○월 ○일 기준"으로 표시) */
    public record OverseasResponse(LocalDate asOf, List<PlayerResponse> players) {
    }

    public record PlayerResponse(Long id, String name, String nameEn, String position, String club, String parentClub,
                                 boolean onLoan, String league, String countryCode, String countryName,
                                 String city, double latitude, double longitude) {
        public static PlayerResponse from(OverseasPlayer p) {
            return new PlayerResponse(p.getId(), p.getName(), p.getNameEn(), p.getPosition().name(), p.getClub(), p.getParentClub(),
                    p.isOnLoan(), p.getLeague(), p.getCountryCode(), p.getCountryName(),
                    p.getCity(), p.getLatitude(), p.getLongitude());
        }
    }
}
