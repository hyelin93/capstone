import { useCallback, useEffect, useState } from 'react'
import { notificationApi } from '../api/notificationApi'
import {
  getFirebaseToken,
  resetFirebaseMessagingCache,
} from '../services/firebaseMessaging'

type PushPermissionStatus =
  | 'unsupported'
  | NotificationPermission
  | 'requesting'
  | 'token-error'

async function registerMessagingServiceWorker() {
  const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js')
  await registration.update()
  return navigator.serviceWorker.ready
}

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

      const serviceWorkerRegistration = await registerMessagingServiceWorker()
      let token = await getFirebaseToken(serviceWorkerRegistration)

      if (!token) {
        await resetFirebaseMessagingCache()
        token = await getFirebaseToken(serviceWorkerRegistration)
      }

      if (!token) {
        setStatus('token-error')
        return
      }

      const username = window.localStorage.getItem('username')
      if (!username) {
        setStatus('token-error')
        return
      }

      console.log('FCM Token:', token)
      await notificationApi.registerPushToken({ username, token })
      setStatus('granted')
    } catch (error) {
      console.error('FCM token registration failed:', error)

      try {
        await resetFirebaseMessagingCache()
        const serviceWorkerRegistration = await registerMessagingServiceWorker()
        const token = await getFirebaseToken(serviceWorkerRegistration)

        if (!token) {
          setStatus('token-error')
          return
        }

        const username = window.localStorage.getItem('username')
        if (!username) {
          setStatus('token-error')
          return
        }

        console.log('FCM Token:', token)
        await notificationApi.registerPushToken({ username, token })
        setStatus('granted')
        return
      } catch (retryError) {
        console.error('FCM token registration retry failed:', retryError)
      }

      setStatus('token-error')
    }
  }, [])

  return {
    status,
    requestPermission,
    isPending: status === 'requesting',
  }
}
