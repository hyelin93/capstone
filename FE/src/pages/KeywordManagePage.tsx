import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'

const initialKeywords = ['모집공고', '나눔', '공모전', '장학금 신청', '취업']

function KeywordManagePage() {
  const [keywords, setKeywords] = useState(initialKeywords)
  const [isRegisterOpen, setIsRegisterOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null)

  const addKeyword = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const formData = new FormData(event.currentTarget)
    const keyword = String(formData.get('keyword') ?? '').trim()

    if (keyword && !keywords.includes(keyword)) {
      setKeywords((currentKeywords) => [...currentKeywords, keyword])
    }
    setIsRegisterOpen(false)
    event.currentTarget.reset()
  }

  const deleteKeyword = () => {
    if (deleteTarget) {
      setKeywords((currentKeywords) => currentKeywords.filter((keyword) => keyword !== deleteTarget))
      setDeleteTarget(null)
    }
  }

  return (
    <main className="phone-page">
      <section className="screen keyword-manage-screen">
        <Link className="back-link" to="/keywords">&lt; 뒤로</Link>
        <h1>키워드 관리</h1>
        <div className="keyword-board">
          {keywords.map((keyword) => (
            <button
              className="keyword-chip"
              key={keyword}
              type="button"
              onClick={() => setDeleteTarget(keyword)}
            >
              {keyword} ×
            </button>
          ))}
        </div>
        <button className="button button-dark full-width" type="button" onClick={() => setIsRegisterOpen(true)}>
          새 키워드 등록하기
        </button>
      </section>

      {isRegisterOpen && (
        <div className="modal-backdrop" role="presentation">
          <form className="modal" onSubmit={addKeyword}>
            <button className="modal-close" type="button" onClick={() => setIsRegisterOpen(false)} aria-label="닫기">
              ×
            </button>
            <h2>새 키워드 등록</h2>
            <div className="modal-row">
              <input name="keyword" placeholder="새로운 키워드" autoFocus />
              <button className="button button-dark" type="submit">등록</button>
            </div>
          </form>
        </div>
      )}

      {deleteTarget && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal">
            <h2>'{deleteTarget}'키워드를 삭제하시겠습니까?</h2>
            <div className="modal-actions">
              <button className="button button-dark" type="button" onClick={deleteKeyword}>삭제</button>
              <button className="button button-muted" type="button" onClick={() => setDeleteTarget(null)}>취소</button>
            </div>
          </div>
        </div>
      )}
    </main>
  )
}

export default KeywordManagePage
