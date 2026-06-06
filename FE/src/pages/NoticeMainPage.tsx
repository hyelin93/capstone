import { Link, useSearchParams } from 'react-router-dom'
import { useNotices } from '../features/notices/queries'

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

function NoticeMainPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const categoryParam = searchParams.get('category')
  const selectedCategory = categoryParam && categories.includes(categoryParam) ? categoryParam : categories[0]

  // '전체' 선택 시 category 필터 없이 전체 조회
  const { data: notices, isLoading, isError } = useNotices(
    selectedCategory === '전체' ? undefined : selectedCategory,
  )

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
        {isLoading && <p className="list-status">불러오는 중...</p>}
        {isError && <p className="list-status">공지를 불러오지 못했습니다.</p>}
        {!isLoading && !isError && notices?.length === 0 && (
          <p className="list-status">공지가 없습니다.</p>
        )}
        <ul className="notice-list">
          {notices?.map((notice) => (
            <li key={notice.id}>
              <Link className="notice-row" to={`/notices/${notice.id}`}>
                <span>{notice.title}</span>
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
