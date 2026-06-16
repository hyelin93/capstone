importScripts('https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.14.1/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: 'AIzaSyALgjMkxYKDvPaE8Dy8_kZEX7oxASGx7uw',
  authDomain: 'capstone-keyword-alert.firebaseapp.com',
  projectId: 'capstone-keyword-alert',
  messagingSenderId: '840461076941',
  appId: '1:840461076941:web:39c6e414f532857cd1dc84',
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  const notification = payload.notification || {};
  const data = payload.data || {};
  const title = notification.title || data.title || '새 공지 알림';
  const options = {
    body: notification.body || data.body || '등록한 키워드가 포함된 공지가 올라왔습니다.',
    data: {
      url: data.url || '/notices',
    },
  };

  self.registration.showNotification(title, options);
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  const url = event.notification.data?.url || '/notices';
  const targetUrl = new URL(url, self.location.origin).href;

  event.waitUntil(
    self.clients
      .matchAll({ type: 'window', includeUncontrolled: true })
      .then((clientList) => {
        for (const client of clientList) {
          if (client.url === targetUrl && 'focus' in client) {
            return client.focus();
          }
        }

        if (self.clients.openWindow) {
          return self.clients.openWindow(targetUrl);
        }
      }),
  );
});
