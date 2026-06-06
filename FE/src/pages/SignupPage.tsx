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
      <section className="auth-panel form-panel" aria-labelledby="signup-title">
        <Link className="back-link" to="/login">&lt; 뒤로</Link>
        <h1 id="signup-title">회원가입</h1>
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
          <label className="field">
            <span>비밀번호 확인</span>
            <input
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              {...register('passwordConfirm', {
                required: true,
                validate: (value) => value === password || '비밀번호가 일치하지 않습니다.',
              })}
            />
          </label>
          {formState.errors.passwordConfirm && (
            <p className="form-error">{formState.errors.passwordConfirm.message}</p>
          )}
          {signup.isError && <p className="form-error">회원가입에 실패했습니다.</p>}
          <button className="button button-dark" type="submit" disabled={signup.isPending}>
            {signup.isPending ? '가입 중...' : '회원가입'}
          </button>
        </form>
      </section>
    </main>
  )
}

export default SignupPage
