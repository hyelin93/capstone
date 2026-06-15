import { RouterProvider } from 'react-router-dom'
import { NotificationBar } from '../features/notifications/components/NotificationBar'
import { Providers } from './providers'
import { router } from './router'

function App() {
  return (
    <Providers>
      <RouterProvider router={router} />
      <NotificationBar />
    </Providers>
  )
}

export default App
