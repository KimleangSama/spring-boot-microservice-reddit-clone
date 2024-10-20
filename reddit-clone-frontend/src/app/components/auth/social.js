import GoogleSVG from '../../../../public/images/google.svg'
import GithubSVG from '../../../../public/images/github.svg'
import { GITHUB_AUTH_URL, GOOGLE_AUTH_URL } from '@/app/constants/base-url'

export default function Social () {
  return (
    <>
      <a href={GOOGLE_AUTH_URL} className='oauthButton'>
        <GoogleSVG className='icon' />
        Continue with Google
      </a>
      <a href={GITHUB_AUTH_URL} className='oauthButton'>
        <GithubSVG className='icon' />
        Continue with Github
      </a>
    </>
  )
}
