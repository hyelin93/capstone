import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useKeywords } from '../features/keywords/queries'
import { useNotices } from '../features/notices/queries'

function KeywordPage() {
  const { data: keywords } = useKeywords()
  const { data: notices } = useNotices()
  const [selected, setSelected] = useState<string>('')

  // 키워드 목록이 로드되면 첫 키워드를 기본 선택
  const activeKeyword = selected || keywords?.[0]?.word || ''

  // 선택된 키워드가 제목에 포함된 공지만 필터링 (키워드별 공지 API 부재 시 클라이언트 필터)
  const matched = activeKeyword
    ? (notices ?? []).filter((notice) => notice.title.includes(activeKeyword))
    : []

  return (
    <main className="phone-page">
      <section className="screen">
        <Link className="back-link" to="/notices">&lt; 뒤로</Link>
        <h1>{matched.length}건의 '{activeKeyword}' 공지 알림</h1>
        <div className="keyword-actions">
          <select
            aria-label="키워드 선택"
            value={activeKeyword}
            onChange={(event) => setSelected(event.target.value)}
          >
            {keywords?.map((keyword) => (
              <option key={keyword.id} value={keyword.word}>
                {keyword.word}
              </option>
            ))}
          </select>
          <Link className="button button-dark" to="/keywords/manage">키워드 관리</Link>
        </div>
        <ul className="notice-list keyword-list">
          {matched.map((notice) => (
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
