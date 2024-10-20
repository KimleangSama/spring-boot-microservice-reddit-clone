import Login from './login';
import SignUp from './signup';
import './styles.css';

const Auth = () => {
    return (
        <div className="wrapper">
            <div className="card-switch">
                <label className="switch">
                    <input type="checkbox" className="toggle" />
                    <span className="slider"></span>
                    <span className="card-side"></span>
                    <div className="flip-card__inner">
                        <Login />
                        <SignUp />
                    </div>
                </label>
            </div>
        </div>
    );
}

export default Auth;