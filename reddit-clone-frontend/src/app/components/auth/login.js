'use client'

import Social from './social'
import { set, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import axios from 'axios'
import { AUTH_URL } from '../../constants/base-url'
import localforage from 'localforage'
import { useEffect, useState } from 'react'
import { redirect } from 'next/navigation'

const schema = z.object({
  email: z
    .string()
    .regex(
      /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      'Invalid email address'
    ),
  password: z
    .string()
    .min(4, 'Password must be at least 4 characters long')
    // .regex(
    //   /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
    //   'Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.'
    // )
})

export default function Login () {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(schema)
  })
  const [accessToken, setAccessToken] = useState(null)

  const onSubmit = formData => {
    const data = JSON.stringify({
      "email": formData.email,
      "password": formData.password
    });

    const config = {
      method: 'post',
      url: AUTH_URL + '/login',
      headers: { 
        'Content-Type': 'application/json'
      },
      data : data
    };
    
    axios.request(config)
    .then((response) => {
      const data = response.data.payload;
      setAccessToken(data.accessToken);
      localforage.setItem('accessToken', data.accessToken);
      localforage.setItem('refreshToken', data.refreshToken);
    })
    .catch((error) => {
      console.log(error);
    });
  }

  useEffect(() => {
    if (accessToken) {
      redirect('/')
    }
  }, [accessToken]);

  return (
    <div className='flip-card__front'>
      <div className='title'>Log In</div>
      <Social />
      <div className='separator'>
        <div></div>
        <span>OR</span>
        <div></div>
      </div>
      <form className='flip-card__form' onSubmit={handleSubmit(onSubmit)}>
        <input
          className='flip-card__input'
          placeholder='Email'
          type='email'
          {...register('email', { required: true, pattern: /^\S+@\S+$/i })}
        />
        {errors.email && (
          <div className='w-full text-left text-red-500 error'>
            {errors.email.message}
          </div>
        )}
        <input
          className='flip-card__input'
          placeholder='Password'
          type='password'
          {...register('password', { required: true })}
        />
        {errors.password && (
          <div className='w-full text-left text-red-500 error'>
            {errors.password.message}
          </div>
        )}
        <button type='submit' className='flip-card__btn'>
          Continue
        </button>
      </form>
    </div>
  )
}
