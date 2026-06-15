import { createBrowserRouter, Navigate } from 'react-router-dom'
import KeywordManagePage from '../pages/KeywordManagePage'
import KeywordPage from '../pages/KeywordPage'
import LoginPage from '../pages/LoginPage'
import NotFoundPage from '../pages/NotFoundPage'
import NoticeDetailPage from '../pages/NoticeDetailPage'
import NoticeMainPage from '../pages/NoticeMainPage'
import SignupPage from '../pages/SignupPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/login" replace />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/signup',
    element: <SignupPage />,
  },
  {
    path: '/notices',
    element: <NoticeMainPage />,
  },
  {
    path: '/notices/:noticeId',
    element: <NoticeDetailPage />,
  },
  {
    path: '/keywords',
    element: <KeywordPage />,
  },
  {
    path: '/keywords/manage',
    element: <KeywordManagePage />,
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
])
