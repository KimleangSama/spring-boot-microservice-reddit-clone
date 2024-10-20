'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { getCurrentUser, logout } from './utils/APIUtils'

export default function Home () {
  const [currentUser, setCurrentUser] = useState(null)
  const [statusCode, setStatusCode] = useState(200)

  useEffect(() => {
    getCurrentUser()
      .then(response => {
        console.log(response)
        setCurrentUser(response)
      })
      .catch(error => {
        console.log(error)
        setCurrentUser(null)
        setStatusCode(error.response?.status)
      })
  }, [])

  if (statusCode == 403) {
    return (
      <div className='flex flex-col justify-center items-center'>
        <h4 className='text-xl text-red-600 font-bold text-center'>
          You don't have permission to see this page.
        </h4>
        <button className='bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-4 rounded my-4'>
          <Link href='/auth'>Login to view this page</Link>
        </button>
      </div>
    )
  } else if (currentUser != null) {
    return (
      <div className='flex flex-col justify-center items-center'>
        <h4 className='text-xl text-green-600 font-bold text-center'>
          Welcome {currentUser.username}
        </h4>
        <button
          onClick={() => {
            logout()
            setCurrentUser(null)
          }}
          className='bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-4 rounded my-4'
        >
          Logout
        </button>
      </div>
    )
  }

  return (
    <div className='flex flex-col justify-center items-center'>
      <h4 className='text-xl text-red-600 font-bold text-center'>
        Restricted Page
      </h4>
      <button className='bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-4 rounded my-4'>
        <Link href='/auth'>Login to view this page</Link>
      </button>
    </div>
  )
}
