import { Link, useSearchParams } from 'react-router-dom'

const categories = [
  '전체',
  '학사공지',
  '행사 공지',
  '생활 공지',
  '취창업 공지',
  '외부공지',
  '추천채용',
  '채용공고',
]

const notices = Array.from({ length: 10 }, (_, index) => ({
  id: String(index + 1),
  title: `학사공지에 있는 글 ${index + 1}`,
  date: '2026.XX.XX',
}))

function NoticeMainPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const categoryParam = searchParams.get('category')
  const selectedCategory = categoryParam && categories.includes(categoryParam) ? categoryParam : categories[0]

  return (
    <main className="phone-page">
      <section className="screen">
        <header className="top-bar">
          <h1>{selectedCategory}</h1>
          <Link className="icon-button" to="/keywords" aria-label="키워드 알림">
            ♧
          </Link>
        </header>
        <label className="category-filter">
          <span>공지 분류</span>
          <select
            value={selectedCategory}
            onChange={(event) => setSearchParams({ category: event.target.value })}
          >
            {categories.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </label>
        <ul className="notice-list">
          {notices.map((notice) => (
            <li key={notice.id}>
              <Link className="notice-row" to={`/notices/${notice.id}`}>
                <span>{selectedCategory}에 있는 글</span>
                <time>{notice.date}</time>
              </Link>
            </li>
          ))}
        </ul>
        <Link className="floating-plus" to="/keywords/manage" aria-label="키워드 관리">
          +
        </Link>
      </section>
    </main>
  )
}

export default NoticeMainPage
