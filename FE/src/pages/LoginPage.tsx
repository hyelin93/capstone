import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { useLogin } from '../features/auth/queries'
import type { LoginRequest } from '../features/auth/types'

function LoginPage() {
  const navigate = useNavigate()
  const login = useLogin()
  const { register, handleSubmit, formState } = useForm<LoginRequest>()

  const onSubmit = (values: LoginRequest) => {
    login.mutate(values, {
      onSuccess: () => {
        window.localStorage.setItem('username', values.username)
        navigate('/notices')
      },
    })
  }

  return (
    <main className="phone-page auth-page">
      <section className="auth-panel login-panel" aria-labelledby="login-title">
        <div className="login-header">
          <h1 id="login-title">학사공지 푸시 알림</h1>
          <p className="login-subtitle">학교 공지사항을 빠르게 받아보세요</p>
        </div>
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="field-group">
            <label className="field">
              <span className="field-label">아이디</span>
              <input
                type="text"
                placeholder="아이디를 입력하세요"
                {...register('username', { required: true })}
              />
            </label>
            <label className="field">
              <span className="field-label">비밀번호</span>
              <input
                type="password"
                placeholder="비밀번호를 입력하세요"
                {...register('password', { required: true })}
              />
            </label>
          </div>
          {login.isError && <p className="form-error">아이디 또는 비밀번호를 확인해주세요.</p>}
          <div className="auth-actions">
            <button
              className="button button-dark login-btn"
              type="submit"
              disabled={login.isPending || formState.isSubmitting}
            >
              {login.isPending ? '로그인 중...' : '로그인'}
            </button>
            <div className="login-divider"><span>또는</span></div>
            <Link className="signup-link" to="/signup">계정이 없으신가요? <strong>회원가입</strong></Link>
          </div>
        </form>
      </section>
    </main>
  )
}

export default LoginPage
