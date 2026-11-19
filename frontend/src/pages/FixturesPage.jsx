import { useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import FixtureBoard from '../components/FixtureBoard';
import FixtureGroups from '../components/FixtureGroups';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { useLeagues } from '../hooks/useLeagues';
import { kstToday } from '../utils/format';

/** 특정 팀의 앞으로 7일 경기 */
function TeamFixtures({ teamId }) {
  const { data, loading, error } = useAsync(async () => {
    const days = await Promise.all([0, 1, 2, 3, 4, 5, 6].map((o) => api.fixtures({ date: kstToday(o), teamId })));
    return days.flat();
  }, [teamId]);
  return (
    <StateBox loading={loading} error={error} empty={!data?.length} emptyText="7일 안에 예정된 경기가 없어요.">
      <FixtureGroups groups={data || []} />
    </StateBox>
  );
}

export default function FixturesPage({ predictMode = false }) {
  const { leagues } = useLeagues();
  const [params] = useSearchParams();
  const teamId = params.get('team');

  return (
    <div className="stack page-narrow">
      <div className="section-head">
        <h1 className="h1">{predictMode ? '승부예측' : teamId ? '내 팀 경기' : '경기일정'}</h1>
      </div>
      {predictMode && <p className="muted">예측할 경기를 골라 결과를 맞혀 보세요. 적중하면 10포인트를 드려요.</p>}
      {teamId ? <TeamFixtures teamId={teamId} /> : (
        <FixtureBoard leagues={leagues} onlyScheduled={predictMode}
                      emptyText={predictMode ? '이 날짜에는 예측할 수 있는 경기가 없어요.' : undefined} />
      )}
    </div>
  );
}
