import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <main className="phone-page auth-page">
      <section className="auth-panel form-panel">
        <h1>페이지를 찾을 수 없습니다</h1>
        <Link className="button button-dark" to="/login">로그인으로 이동</Link>
      </section>
    </main>
  )
}

export default NotFoundPage
