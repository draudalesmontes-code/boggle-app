import { useState } from "react";
import { Card, Form, Button } from "react-bootstrap";
import { useNavigate, Link } from "react-router-dom";
import "./LoginPage.css";

const USERNAME_REGEX = /^[a-zA-Z0-9_]+$/; // Allows only letters, numbers, and underscores
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_USERNAME_LENGTH = 5;
const MIN_PASSWORD_LENGTH = 7;
const STORAGE_KEY = "bbUser";

/**
 * LoginPage is the login page that includes a field for username, email, and password that has
 * several checks from frontend and from API before authenticating and storing user info.
 * Page routes to HomePage and SignupPage
 */
export default function LoginPage() {
    const [usernameInput, setUsernameInput] = useState("");
    const [emailInput, setEmailInput] = useState("");
    const [passwordInput, setPasswordInput] = useState("");
    const [usernameError, setUsernameError] = useState(false);
    const navigate = useNavigate();

    /**
     * Validation for login input and sends the login request
     * On success, stores returned user to localStorage and navigate to home page
     * On fail, display an alert
     */
    function handleLogin(e) {
        e.preventDefault();
        const username = usernameInput.trim();
        const email = emailInput.trim();
        const password = passwordInput.trim();

        // Basic frontend validation for needed login fields
        if (!username || !email || !password) {
            alert("Please enter a username, email, and password.");
            return;
        }
        if (username.length < MIN_USERNAME_LENGTH) {
            alert(`Username should be at least ${MIN_USERNAME_LENGTH} characters.`);
            return;
        }
        if (!EMAIL_REGEX.test(emailInput)) {
            alert("Invalid email");
            return;
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            alert(`Password should be at least ${MIN_PASSWORD_LENGTH} characters.`);
            return;
        }

        // Basic startup call to API, currently not working due to CORS violation but implemented
        fetch(`/api/users/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                email: email,
                password: password
            })
        }).then(res => {
            if (res.status === 401) {
                alert("Invalid username or password!");
                return null;
            }
            if (res.status !== 200) {
                alert("Something went wrong");
                return null;
            }
            return res.json();
        }).then(data => {
            if (!data) return;

            saveAccount(data);
            alert(`Logged in as ${data.username}`);
            navigate("/home");
        });
    }

    /**
     * Sends the guest login request
     * On success, stores returned guest info in localStorage and navigate to home page
     */
    function handleGuest() {
        const username = usernameInput.trim();

        fetch(`/api/users/guest`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username
            })
        }).then(res => {
            if (res.status === 409) {
                alert("Username or email already taken!");
                return null;
            }
            if(res.status !== 201) {
                alert("Something went wrong");
                return null;
            }
            return res.json();
        }).then(data => {
            if (!data) return;

            saveAccount(data);
            alert(`Logged in as guest ${data.username}`);
            navigate("/home");
        });
    }

    /**
     * Stores authenticated user data in localStorage
     * @param {*} account the authenticated user object
     */
    function saveAccount(account) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(account));
    }

    return (
        <div className="login-wrap">
            <img
                src="logo.png"
                alt="An image of the Boggle Buddies logo"
                style={{ width: 300 }}
            />
            <h1 className="login-title">Ready to play?</h1>
            <Card className="login-card">
                <Form onSubmit={handleLogin}>
                    <Form.Group>
                        <Form.Label>Username:</Form.Label>
                        <Form.Control
                            className="bb-input"
                            value={usernameInput}
                            onChange={e => {
                                const value = e.target.value;
                                setUsernameInput(value);
                                setUsernameError(!USERNAME_REGEX.test(value));
                            }}
                            placeholder="Enter your username"
                            isInvalid={usernameError}
                        />
                        {usernameError && (
                            <div className="bb-error">
                                Only letters, numbers, and underscores allowed.
                            </div>
                        )}
                    </Form.Group>
                    <Form.Group>
                        <Form.Label>Email:</Form.Label>
                        <Form.Control
                            className="bb-input"
                            value={emailInput}
                            onChange={e => setEmailInput(e.target.value)}
                            placeholder="Enter your email e.g. name@example.com "
                        />
                    </Form.Group>
                    <Form.Group>
                        <Form.Label>Password:</Form.Label>
                        <Form.Control
                            className="bb-input"
                            value={passwordInput}
                            onChange={e => setPasswordInput(e.target.value)}
                            placeholder="Enter your password"
                            type="password"
                        />
                    </Form.Group>
                    <Button
                        className="btn-primary"
                        disabled={!usernameInput || !emailInput || !passwordInput || usernameError}
                        type="submit"
                    >Log in</Button>
                </Form>
                <Button className="btn-secondary" onClick={handleGuest}>Play as guest</Button>
                <div>
                    New here?{" "}
                    <Link to="/signup">Create an account</Link>
                </div>
            </Card>
        </div>
    );
}