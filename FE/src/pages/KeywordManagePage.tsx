import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import type { Keyword } from '../features/keywords/types'
import { useCreateKeyword, useDeleteKeyword, useKeywords } from '../features/keywords/queries'

function KeywordManagePage() {
  const { data: keywords, isLoading, isError } = useKeywords()
  const createKeyword = useCreateKeyword()
  const deleteKeyword = useDeleteKeyword()

  const [isRegisterOpen, setIsRegisterOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<Keyword | null>(null)

  const addKeyword = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const form = event.currentTarget
    const formData = new FormData(form)
    const keyword = String(formData.get('keyword') ?? '').trim()
    const exists = keywords?.some((item) => item.word === keyword)

    if (!keyword || exists) {
      setIsRegisterOpen(false)
      return
    }

    createKeyword.mutate(keyword, {
      onSuccess: () => {
        setIsRegisterOpen(false)
        form.reset()
      },
    })
  }

  const confirmDelete = () => {
    if (!deleteTarget) return
    deleteKeyword.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteTarget(null),
    })
  }

  return (
    <main className="phone-page">
      <section className="screen keyword-manage-screen">
        <Link className="back-link" to="/keywords">&lt; 뒤로</Link>
        <h1>키워드 관리</h1>
        {isLoading && <p className="list-status">불러오는 중...</p>}
        {isError && <p className="list-status">키워드를 불러오지 못했습니다.</p>}
        <div className="keyword-board">
          {keywords?.map((keyword) => (
            <button
              className="keyword-chip"
              key={keyword.id}
              type="button"
              onClick={() => setDeleteTarget(keyword)}
            >
              {keyword.word} ×
            </button>
          ))}
        </div>
        <button
          className="button button-dark full-width"
          type="button"
          onClick={() => setIsRegisterOpen(true)}
        >
          새 키워드 등록하기
        </button>
      </section>

      {isRegisterOpen && (
        <div className="modal-backdrop" role="presentation">
          <form className="modal" onSubmit={addKeyword}>
            <button
              className="modal-close"
              type="button"
              onClick={() => setIsRegisterOpen(false)}
              aria-label="닫기"
            >
              ×
            </button>
            <h2>새 키워드 등록</h2>
            <div className="modal-row">
              <input name="keyword" placeholder="새로운 키워드" autoFocus />
              <button className="button button-dark" type="submit" disabled={createKeyword.isPending}>
                {createKeyword.isPending ? '등록 중...' : '등록'}
              </button>
            </div>
          </form>
        </div>
      )}

      {deleteTarget && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal">
            <h2>'{deleteTarget.word}'키워드를 삭제하시겠습니까?</h2>
            <div className="modal-actions">
              <button
                className="button button-dark"
                type="button"
                onClick={confirmDelete}
                disabled={deleteKeyword.isPending}
              >
                {deleteKeyword.isPending ? '삭제 중...' : '삭제'}
              </button>
              <button className="button button-muted" type="button" onClick={() => setDeleteTarget(null)}>
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  )
}

export default KeywordManagePage
