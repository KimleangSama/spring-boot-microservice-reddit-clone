import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom'
import Login from './components/auth/Login'
import Home from './components/Home'
import Register from './components/auth/Register'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route index path='/' element={<Home />} />
        <Route path='/login' element={<Login />} />
        <Route path='/register' element={<Register />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
