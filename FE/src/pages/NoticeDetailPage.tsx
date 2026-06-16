import { Link, useParams } from 'react-router-dom'
import { useNotice } from '../features/notices/queries'

function NoticeDetailPage() {
  const { noticeId } = useParams()
  const id = Number(noticeId)
  const { data: notice, isLoading, isError } = useNotice(id)

  return (
    <main className="phone-page detail-page">
      <article className="screen detail-screen">
        <div className="detail-scroll-area">
          <Link className="back-link" to="/notices">&lt; 뒤로</Link>
          {isLoading && <p className="list-status">불러오는 중...</p>}
          {isError && <p className="list-status">공지를 불러오지 못했습니다.</p>}
          {notice && (
            <>
              <h1>{notice.title}</h1>
              <div className="detail-meta">
                <strong>{notice.category}</strong>
                <time>{notice.date}</time>
              </div>
              <div className="detail-body">
                {notice.content || '내용이 없습니다.'}
              </div>
            </>
          )}
        </div>
        {notice && (
          <div className="detail-footer">
            <a className="button button-dark" href={notice.link} target="_blank" rel="noreferrer">
              원문 보기
            </a>
          </div>
        )}
      </article>
    </main>
  )
}

export default NoticeDetailPage
