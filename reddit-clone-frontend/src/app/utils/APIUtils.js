import axios from 'axios'
import { AUTH_URL } from '../constants/base-url'
import localforage from 'localforage'

export async function getCurrentUser () {
  const accessToken = await localforage.getItem('accessToken')
  console.log(accessToken)
  if (!accessToken) {
    return Promise.reject('No access token set.')
  } else {
    return axios.get(AUTH_URL + '/user/me', {
      headers: {
        Authorization: 'Bearer ' + accessToken,
        'Content-Type': 'application/json'
      }
    })
  }
}

export async function logout () {
  const accessToken = await localforage.getItem('accessToken')
  const refreshToken = await localforage.getItem('refreshToken')
  if (!accessToken || !refreshToken) {
    return Promise.reject('No access token or refresh token set.')
  } else {
    await localforage.removeItem('accessToken')
    await localforage.removeItem('refreshToken')
    return axios.post(AUTH_URL + '/logout', {
      accessToken: accessToken,
      refreshToken: refreshToken
    })
  }
}
