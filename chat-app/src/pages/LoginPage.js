import React, { useContext, useEffect, useState } from 'react';
import { Link }        from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import Swal            from 'sweetalert2';

/**
 * Login page component.
 * Supports email/password login and a "Remember me" option that
 * persists the email address in localStorage for future visits.
 */
export const LoginPage = () => {
    const { login } = useContext(AuthContext);

    const [form, setForm] = useState({
        email:      '',
        password:   '',
        rememberMe: false,
    });

    // Pre-fill the email field if the user previously chose "Remember me".
    useEffect(() => {
        const savedEmail = localStorage.getItem('email');
        if (savedEmail) {
            setForm((prev) => ({ ...prev, rememberMe: true, email: savedEmail }));
        }
    }, []);

    /** Syncs text inputs with form state. */
    const onChange = ({ target }) => {
        const { name, value } = target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    /** Toggles the "Remember me" checkbox. */
    const toggleRememberMe = () => {
        setForm((prev) => ({ ...prev, rememberMe: !prev.rememberMe }));
    };

    /** Handles form submission — saves or removes the email, then calls login. */
    const onSubmit = async (e) => {
        e.preventDefault();

        if (form.rememberMe) {
            localStorage.setItem('email', form.email);
        } else {
            localStorage.removeItem('email');
        }

        const result = await login(form.email, form.password);
        if (result !== true) {
            Swal.fire('Error', result, 'error');
        }
    };

    /** Returns true only when both fields have content. */
    const isFormValid = () => form.email.length > 0 && form.password.length > 0;

    return (
        <div>
            <form
                className="login100-form validate-form flex-sb flex-w"
                onSubmit={onSubmit}
            >
                <span className="login100-form-title mb-3">Chat - Login</span>

                {/* Email field */}
                <div className="wrap-input100 validate-input mb-3">
                    <input
                        className="input100"
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={form.email}
                        onChange={onChange}
                    />
                    <span className="focus-input100" />
                </div>

                {/* Password field */}
                <div className="wrap-input100 validate-input mb-3">
                    <input
                        className="input100"
                        type="password"
                        name="password"
                        placeholder="Password"
                        value={form.password}
                        onChange={onChange}
                    />
                    <span className="focus-input100" />
                </div>

                {/* Remember me + Register link */}
                <div className="row mb-3">
                    <div className="col">
                        <input
                            className="input-checkbox100"
                            id="ckb1"
                            type="checkbox"
                            name="rememberMe"
                            checked={form.rememberMe}
                            onChange={toggleRememberMe}
                        />
                        <label className="label-checkbox100">Remember me</label>
                    </div>

                    <div className="col text-right">
                        <Link to="/auth/register" className="txt1">
                            New account?
                        </Link>
                    </div>
                </div>

                {/* Submit button */}
                <div className="container-login100-form-btn m-t-17">
                    <button
                        type="submit"
                        className="login100-form-btn"
                        disabled={!isFormValid()}
                    >
                        Sign in
                    </button>
                </div>
            </form>
        </div>
    );
};
