import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { CATEGORY_LABEL, num, relativeTime } from '../utils/format';

export default function PostDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data: post, loading, error } = useAsync(() => api.post(id), [id]);
  const comments = useAsync(() => api.comments(id), [id]);
  const [text, setText] = useState('');
  const [msg, setMsg] = useState('');

  const remove = async () => {
    if (!window.confirm('게시글을 삭제할까요?')) return;
    await api.deletePost(id);
    navigate('/community');
  };

  const addComment = async (e) => {
    e.preventDefault();
    if (!text.trim()) return;
    try { await api.createComment(id, text.trim()); setText(''); comments.reload(); }
    catch (err) { setMsg(err.message); }
  };

  const removeComment = async (cid) => { await api.deleteComment(cid); comments.reload(); };

  return (
    <div className="stack page-narrow">
      <Link to="/community" className="more">← 목록으로</Link>
      <StateBox loading={loading} error={error}>
        {post && (
          <article className="card post">
            <span className="cat">{CATEGORY_LABEL[post.category]}</span>
            <h1 className="h1">{post.title}</h1>
            <p className="muted small">{post.authorNickname} · 조회 {num(post.viewCount)} · {relativeTime(post.createdAt)}</p>
            <div className="post__body">{post.content}</div>
            {user?.id === post.authorId && (
              <div className="post__actions">
                <Link to={`/community/${id}/edit`} className="btn btn--ghost">수정</Link>
                <button type="button" className="btn btn--ghost" onClick={remove}>삭제</button>
              </div>
            )}
          </article>
        )}
      </StateBox>

      <section className="card post" aria-label="댓글">
        <h2 className="h3">댓글 {comments.data?.length ?? 0}</h2>
        {comments.data?.map((c) => (
          <div className="comment" key={c.id}>
            <div><strong>{c.authorNickname}</strong> <span className="muted small">{relativeTime(c.createdAt)}</span></div>
            <p>{c.content}</p>
            {user?.id === c.authorId && <button type="button" className="link-btn" onClick={() => removeComment(c.id)}>삭제</button>}
          </div>
        ))}
        {user ? (
          <form className="comment-form" onSubmit={addComment}>
            <label className="sr-only" htmlFor="comment">댓글</label>
            <textarea id="comment" rows={3} maxLength={1000} value={text} onChange={(e) => setText(e.target.value)} placeholder="댓글을 남겨 주세요" />
            <button type="submit" className="btn btn--primary">등록</button>
          </form>
        ) : <p className="muted"><Link to="/login">로그인</Link>하면 댓글을 쓸 수 있어요.</p>}
        {msg && <p className="error-text">{msg}</p>}
      </section>
    </div>
  );
}
