import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { useSignup } from '../features/auth/queries'

interface SignupForm {
  username: string
  password: string
  passwordConfirm: string
}

function SignupPage() {
  const navigate = useNavigate()
  const signup = useSignup()
  const { register, handleSubmit, watch, formState } = useForm<SignupForm>()
  const password = watch('password')

  const onSubmit = ({ username, password }: SignupForm) => {
    signup.mutate(
      { username, password },
      {
        onSuccess: () => navigate('/login'),
      },
    )
  }

  return (
    <main className="phone-page auth-page">
      <section className="auth-panel login-panel" aria-labelledby="signup-title">
        <div className="login-header">
          <h1 id="signup-title">회원가입</h1>
        
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
            <label className="field">
              <span className="field-label">비밀번호 확인</span>
              <input
                type="password"
                placeholder="비밀번호를 다시 입력하세요"
                {...register('passwordConfirm', {
                  required: true,
                  validate: (value) => value === password || '비밀번호가 일치하지 않습니다.',
                })}
              />
            </label>
          </div>
          {formState.errors.passwordConfirm && (
            <p className="form-error">{formState.errors.passwordConfirm.message}</p>
          )}
          {signup.isError && <p className="form-error">회원가입에 실패했습니다.</p>}
          <div className="auth-actions">
            <button className="button button-dark login-btn" type="submit" disabled={signup.isPending}>
              {signup.isPending ? '가입 중...' : '회원가입'}
            </button>
            <div className="login-divider"><span>또는</span></div>
            <Link className="signup-link" to="/login">이미 계정이 있으신가요? <strong>로그인</strong></Link>
          </div>
        </form>
      </section>
    </main>
  )
}

export default SignupPage
