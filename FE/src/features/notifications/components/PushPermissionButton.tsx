import { usePushPermission } from '../hooks/usePushPermission'

export function PushPermissionButton() {
  const { status, requestPermission, isPending } = usePushPermission()

  if (status === 'unsupported') {
    return <p className="form-error">이 브라우저에서는 알림을 사용할 수 없습니다.</p>
  }

  if (status === 'granted') {
    return (
      <button
        className="push-status push-status-button"
        type="button"
        onClick={requestPermission}
        disabled={isPending}
      >
        {isPending ? '알림 재설정 중...' : '알림 설정 완료'}
      </button>
    )
  }

  if (status === 'denied') {
    return <p className="form-error">브라우저 설정에서 알림을 허용해주세요.</p>
  }

  return (
    <button
      className="button button-dark full-width"
      type="button"
      onClick={requestPermission}
      disabled={isPending}
    >
      {isPending ? '알림 설정 중...' : status === 'token-error' ? '알림 다시 설정' : '알림 설정'}
    </button>
  )
}
