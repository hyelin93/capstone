import { initializeApp } from "firebase/app";
import { getMessaging, getToken, isSupported } from "firebase/messaging";

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

const firebaseApp = initializeApp(firebaseConfig);

export const getFirebaseMessaging = async () => {
  const supported = await isSupported();

  if (!supported) {
    return null;
  }

  return getMessaging(firebaseApp);
};

export const getFirebaseToken = async (
  serviceWorkerRegistration?: ServiceWorkerRegistration,
) => {
  const messaging = await getFirebaseMessaging();

  if (!messaging) {
    return null;
  }

  return getToken(messaging, {
    vapidKey: import.meta.env.VITE_FIREBASE_VAPID_KEY,
    serviceWorkerRegistration,
  });
};
