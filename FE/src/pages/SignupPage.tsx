import { Link } from 'react-router-dom'

function SignupPage() {
  return (
    <main className="phone-page auth-page">
      <section className="auth-panel form-panel" aria-labelledby="signup-title">
        <Link className="back-link" to="/login">&lt; 뒤로</Link>
        <h1 id="signup-title">회원가입</h1>
        <label className="field">
          <span>아이디</span>
          <input type="text" placeholder="아이디를 입력하세요" />
        </label>
        <label className="field">
          <span>비밀번호</span>
          <input type="password" placeholder="비밀번호를 입력하세요" />
        </label>
        <label className="field">
          <span>비밀번호 확인</span>
          <input type="password" placeholder="비밀번호를 다시 입력하세요" />
        </label>
        <Link className="button button-dark" to="/notices">회원가입</Link>
      </section>
    </main>
  )
}

export default SignupPage
