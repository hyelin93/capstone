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
      <section className="auth-panel form-panel" aria-labelledby="login-title">
        <h1 id="login-title">학사공지<br />푸시 알림</h1>
        <form onSubmit={handleSubmit(onSubmit)}>
          <label className="field">
            <span>아이디</span>
            <input
              type="text"
              placeholder="아이디를 입력하세요"
              {...register('username', { required: true })}
            />
          </label>
          <label className="field">
            <span>비밀번호</span>
            <input
              type="password"
              placeholder="비밀번호를 입력하세요"
              {...register('password', { required: true })}
            />
          </label>
          {login.isError && <p className="form-error">로그인에 실패했습니다.</p>}
          <div className="auth-actions">
            <button
              className="button button-muted"
              type="submit"
              disabled={login.isPending || formState.isSubmitting}
            >
              {login.isPending ? '로그인 중...' : '로그인'}
            </button>
            <Link className="button button-dark" to="/signup">회원가입</Link>
          </div>
        </form>
      </section>
    </main>
  )
}

export default LoginPage
