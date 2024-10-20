export default function SignUp () {
  return (
    <div className='flip-card__back'>
      <div className='title'>Sign Up</div>
      <form className='flip-card__form' action=''>
        <input className='flip-card__input' placeholder='Name' type='name' />
        <input
          className='flip-card__input'
          name='email'
          placeholder='Email'
          type='email'
        />
        <input
          className='flip-card__input'
          name='password'
          placeholder='Password'
          type='password'
        />
        <button className='flip-card__btn'>Confirm</button>
      </form>
    </div>
  )
}
