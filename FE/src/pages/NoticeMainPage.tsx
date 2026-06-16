import { Link, useSearchParams } from 'react-router-dom'
import { useInfiniteNotices } from '../features/notices/queries'
import icon from '../assets/Bell.png'

const categories = [
  { label: '전체', value: '' },
  { label: '학사공지', value: '학사' },
  { label: '행사 공지', value: '행사' },
  { label: '생활 공지', value: '생활' },
  { label: '취창업 공지', value: '취창업' },
  { label: '외부공지', value: '외부' },
  { label: '추천채용', value: '추천채용' },
  { label: '채용공고', value: '채용공고' },
]

function NoticeMainPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const categoryParam = searchParams.get('category')
  const selectedCategory = categories.find((c) => c.value === (categoryParam ?? '')) ?? categories[0]

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isError,
    isFetchingNextPage,
    isLoading,
  } = useInfiniteNotices(selectedCategory.value || undefined)
  const notices = Array.from(
    new Map((data?.pages.flat() ?? []).map((notice) => [notice.id, notice])).values(),
  )

  return (
    <main className="phone-page notice-page">
      <section className="screen">
        <header className="top-bar">
          <h1>{selectedCategory.label}</h1>
          <Link className="icon-button" to="/keywords" aria-label="키워드 알림">
            <img src={icon} alt="키워드 알림" width="20" />
          </Link>
        </header>
        <label className="category-filter">
          <span>공지 분류</span>
          <select
            value={selectedCategory.value}
            onChange={(event) => {
              const nextCategory = event.target.value
              setSearchParams(nextCategory ? { category: nextCategory } : {})
            }}
          >
            {categories.map((category) => (
              <option key={category.value || 'all'} value={category.value}>
                {category.label}
              </option>
            ))}
          </select>
        </label>

        <div className="notice-scroll-area">
          {isLoading && <p className="list-status">불러오는 중...</p>}
          {isError && <p className="list-status">공지를 불러오지 못했습니다.</p>}
          {!isLoading && !isError && notices.length === 0 && (
            <p className="list-status">공지가 없습니다.</p>
          )}
          <ul className="notice-list">
            {notices.map((notice, index) => (
              <li key={`${notice.id}-${index}`}>
                <Link className="notice-row" to={`/notices/${notice.id}`}>
                  <div className="notice-row-content">
                    <span>{notice.title}</span>
                    <time>{notice.date}</time>
                  </div>
                  <span className="notice-row-arrow">›</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
        <footer className="notice-footer">
          {hasNextPage ? (
            <button
              className="notice-more-btn"
              type="button"
              onClick={() => fetchNextPage()}
              disabled={isFetchingNextPage}
            >
              {isFetchingNextPage ? '불러오는 중...' : '더보기'}
            </button>
          ) : (
            notices.length > 0 && <p className="notice-end-text">마지막 공지입니다.</p>
          )}
        </footer>
      </section>
    </main>
  )
}

export default NoticeMainPage
