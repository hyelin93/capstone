import { Link } from 'react-router-dom'

const keywordNotices = Array.from({ length: 10 }, (_, index) => ({
  id: index + 1,
  title: `${index % 2 === 0 ? '모집공고' : '장학공지'} 관련 공지 알림입니다...`,
  date: '2026.XX.XX',
}))

function KeywordPage() {
  return (
    <main className="phone-page">
      <section className="screen">
        <Link className="back-link" to="/notices">&lt; 뒤로</Link>
        <h1>N건의 '모집공고' 공지 알림</h1>
        <div className="keyword-actions">
          <select aria-label="키워드 선택">
            <option>모집공고</option>
            <option>장학금</option>
            <option>수강신청</option>
          </select>
          <Link className="button button-dark" to="/keywords/manage">키워드 관리</Link>
        </div>
        <ul className="notice-list keyword-list">
          {keywordNotices.map((notice) => (
            <li key={notice.id}>
              <Link className="notice-row" to={`/notices/${notice.id}`}>
                <span>{notice.title}</span>
                <time>{notice.date}</time>
              </Link>
            </li>
          ))}
        </ul>
      </section>
    </main>
  )
}

export default KeywordPage
