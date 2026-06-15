importScripts('https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.14.1/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: 'AIzaSyAPQhBxsF1VfSIVBFX1X6acecy2wi6iG3c',
  authDomain: 'keyword-ae4db.firebaseapp.com',
  projectId: 'keyword-ae4db',
  messagingSenderId: '19026335038',
  appId: '1:19026335038:web:93a69b5fcd86612597486f',
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
