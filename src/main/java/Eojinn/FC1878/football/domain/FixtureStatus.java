package Eojinn.FC1878.football.domain;

/** 경기 상태 (football-data.org 상태 값과의 변환은 FootballSyncService.statusOf) */
public enum FixtureStatus {
    SCHEDULED, LIVE, FINISHED, POSTPONED, CANCELLED
}
