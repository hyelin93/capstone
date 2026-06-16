import { useEffect, useState } from 'react'
import { subscribeForegroundMessage } from '../services/firebaseMessaging'

interface NotificationItem {
  title: string
  body: string
  url: string
}

const DEFAULT_NOTIFICATION: NotificationItem = {
  title: '새 공지 알림',
  body: '등록한 키워드가 포함된 공지가 올라왔습니다.',
  url: '/notices',
}

function normalizeNotification(value: Partial<NotificationItem>): NotificationItem {
  return {
    title: value.title || DEFAULT_NOTIFICATION.title,
    body: value.body || DEFAULT_NOTIFICATION.body,
    url: value.url || DEFAULT_NOTIFICATION.url,
  }
}

export function NotificationBar() {
  const [notification, setNotification] = useState<NotificationItem | null>(null)

  useEffect(() => {
    let unsubscribe: (() => void) | null = null
    let active = true

    subscribeForegroundMessage((payload) => {
      if (!active) {
        return
      }

      console.log('FCM foreground message:', payload)

      setNotification(
        normalizeNotification({
          title: payload.notification?.title || payload.data?.title || payload.data?.noticeTitle,
          body:
            payload.notification?.body ||
            payload.data?.body ||
            payload.data?.message ||
            payload.data?.content ||
            payload.data?.noticeContent,
          url: payload.data?.url || payload.data?.link,
        }),
      )
    }).then((cleanup) => {
      unsubscribe = cleanup
    })

    return () => {
      active = false
      unsubscribe?.()
    }
  }, [])

  useEffect(() => {
    if (!import.meta.env.DEV) {
      return
    }

    const showTestNotification = (event: Event) => {
      const detail = (event as CustomEvent<Partial<NotificationItem>>).detail || {}
      setNotification(normalizeNotification(detail))
    }

    window.addEventListener('test-notification', showTestNotification)

    return () => {
      window.removeEventListener('test-notification', showTestNotification)
    }
  }, [])

  useEffect(() => {
    if (!notification) {
      return
    }

    const timeoutId = window.setTimeout(() => {
      setNotification(null)
    }, 6000)

    return () => window.clearTimeout(timeoutId)
  }, [notification])

  if (!notification) {
    return null
  }

  const openNotification = () => {
    window.location.assign(notification.url)
  }

  return (
    <aside className="notification-bar" role="status" aria-live="polite">
      <button className="notification-content" type="button" onClick={openNotification}>
        <strong>{notification.title}</strong>
        <span>{notification.body}</span>
      </button>
      <button
        className="notification-close"
        type="button"
        onClick={() => setNotification(null)}
        aria-label="알림 닫기"
      >
        x
      </button>
    </aside>
  )
}
