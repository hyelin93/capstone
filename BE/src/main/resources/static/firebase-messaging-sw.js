importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

firebase.initializeApp({
    apiKey: "AIzaSyAPQhBxsF1VfSIVBFX1X6acecy2wi6iG3c",
    projectId: "keyword-ae4db",
    messagingSenderId: "19026335038",
    appId: "1:19026335038:web:93a69b5fcd86612597486f"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log("백그라운드 메시지:", payload);

    self.registration.showNotification(
        payload.notification.title,
        {
            body: payload.notification.body,
            icon: "/firebase-logo.png"
        }
    );
});