import { Link } from 'react-router-dom'

function LoginPage() {
  return (
    <main className="phone-page auth-page">
      <section className="auth-panel" aria-labelledby="login-title">
        <h1 id="login-title">학사공지<br />푸시 알림</h1>
        <div className="auth-actions">
          <Link className="button button-muted" to="/notices">로그인</Link>
          <Link className="button button-dark" to="/signup">회원가입</Link>
        </div>
      </section>
    </main>
  )
}

export default LoginPage
