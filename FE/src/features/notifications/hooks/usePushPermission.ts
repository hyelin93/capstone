import { useCallback, useEffect, useState } from 'react'
import { notificationApi } from '../api/notificationApi'
import { getFirebaseToken } from '../services/firebaseMessaging'

type PushPermissionStatus =
  | 'unsupported'
  | NotificationPermission
  | 'requesting'
  | 'token-error'

export function usePushPermission() {
  const [status, setStatus] = useState<PushPermissionStatus>('default')

  useEffect(() => {
    if (!('Notification' in window) || !('serviceWorker' in navigator)) {
      setStatus('unsupported')
      return
    }

    setStatus(Notification.permission)
  }, [])

  const requestPermission = useCallback(async () => {
    if (!('Notification' in window) || !('serviceWorker' in navigator)) {
      setStatus('unsupported')
      return
    }

    try {
      setStatus('requesting')

      const permission =
        Notification.permission === 'default'
          ? await Notification.requestPermission()
          : Notification.permission

      if (permission !== 'granted') {
        setStatus(permission)
        return
      }

      const serviceWorkerRegistration = await navigator.serviceWorker.register(
        '/firebase-messaging-sw.js',
      )
      const token = await getFirebaseToken(serviceWorkerRegistration)

      if (!token) {
        setStatus('token-error')
        return
      }

      await notificationApi.registerPushToken({ token })
      setStatus('granted')
    } catch {
      setStatus('token-error')
    }
  }, [])

  return {
    status,
    requestPermission,
    isPending: status === 'requesting',
  }
}
