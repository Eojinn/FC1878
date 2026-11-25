/*
 * 선수 커리어 (출처: Wikidata). 소속팀 이력을 위에서 아래로 시간순 타임라인으로 보여줌.
 */

/** "2015-08" → "2015.08", "2015" → "2015" */
const ym = (d) => (d ? d.replace('-', '.') : null);

function period(e) {
  const start = ym(e.start) ?? '?';
  if (e.current) return `${start} ~ 현재`;
  const end = ym(e.end);
  return end && end !== start ? `${start} ~ ${end}` : start;
}

function CareerTable({ title, entries, appsLabel = '경기' }) {
  if (!entries.length) return null;
  return (
    <div className="career-block">
      <h3 className="career-block__title">{title}</h3>
      <ol className="career-timeline">
        {entries.map((e, i) => (
          <li key={`${e.teamEn ?? e.team}-${e.start}-${i}`} className={e.current ? 'is-current' : ''}>
            <span className="career-timeline__dot" aria-hidden="true" />
            <span className="career-timeline__period num">{period(e)}</span>
            <span className="career-timeline__team">
              <strong>{e.team}</strong>
              {e.teamEn && e.teamEn !== e.team && <span className="muted small">{e.teamEn}</span>}
            </span>
            <span className="career-timeline__tags">
              {e.loan && <span className="tr-type tr-type--loan">임대</span>}
              {e.current && <span className="tr-type tr-type--free">현재</span>}
            </span>
            <span className="career-timeline__stat num" title={appsLabel}>{e.apps ?? '-'}<small>{appsLabel}</small></span>
            <span className="career-timeline__stat num" title="골">{e.goals ?? '-'}<small>골</small></span>
          </li>
        ))}
      </ol>
    </div>
  );
}

/** data: /api/players/{id}/career 또는 /api/career 응답 */
export default function CareerSection({ data, loading, error }) {
  return (
    <section className="card player-stats career" aria-label="커리어">
      <div className="career__head">
        <h2 className="h3">커리어</h2>
        {data?.found && (data.clubApps != null || data.clubGoals != null) && (
          <span className="muted small">클럽 통산 <strong className="num">{data.clubApps ?? '-'}</strong>경기 <strong className="num">{data.clubGoals ?? '-'}</strong>골</span>
        )}
      </div>

      {loading && <p className="muted small">커리어를 불러오는 중… (처음 볼 때는 몇 초 걸릴 수 있어요)</p>}
      {!loading && error && <p className="muted small">커리어를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.</p>}
      {!loading && !error && data && !data.found && (
        <p className="muted small">공개 데이터(Wikidata)에서 이 선수의 커리어를 찾지 못했어요. 유명하지 않은 선수이거나 이름 표기가 다를 수 있어요.</p>
      )}

      {!loading && data?.found && (
        <>
          <CareerTable title="클럽" entries={data.clubs} />
          <CareerTable title="국가대표" entries={data.nationalTeams} appsLabel="A매치" />
          {!data.clubs.length && !data.nationalTeams.length && <p className="muted small">등록된 소속팀 이력이 없어요.</p>}
          <p className="muted small career__source">
            출처: <a href={data.sourceUrl} target="_blank" rel="noopener noreferrer">Wikidata</a> (누구나 편집하는 공개 데이터, CC0).
            경기·골은 기록이 있는 구간만 나오고, 최신 소식과 다를 수 있어요.
          </p>
        </>
      )}
    </section>
  );
}
