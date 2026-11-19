import { useEffect, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import { CATEGORY_LABEL } from '../utils/format';

export default function PostWritePage() {
  const { id } = useParams();
  const { user, loading } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ category: 'FREE', title: '', content: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (id) api.post(id).then((p) => setForm({ category: p.category, title: p.title, content: p.content }));
  }, [id]);

  if (!loading && !user) return <Navigate to="/login" state={{ from: '/community/write' }} replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (id) { await api.updatePost(id, form); navigate(`/community/${id}`); }
      else { const { id: newId } = await api.createPost(form); navigate(`/community/${newId}`); }
    } catch (err) {
      setError(Object.values(err.body?.fieldErrors || {})[0] || err.message);
    }
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <form className="card form page-narrow" onSubmit={submit}>
      <h1 className="h1">{id ? '글 수정' : '글쓰기'}</h1>
      <label className="field">
        <span>카테고리</span>
        <select value={form.category} onChange={set('category')}>
          {Object.entries(CATEGORY_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </label>
      <label className="field">
        <span>제목</span>
        <input required maxLength={200} value={form.title} onChange={set('title')} />
      </label>
      <label className="field">
        <span>내용</span>
        <textarea required rows={12} value={form.content} onChange={set('content')} />
      </label>
      {error && <p className="error-text">{error}</p>}
      <button type="submit" className="btn btn--primary btn--block">{id ? '수정하기' : '등록하기'}</button>
    </form>
  );
}
