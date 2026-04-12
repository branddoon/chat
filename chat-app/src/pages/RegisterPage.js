import React, { useContext, useState } from 'react';
import { Link }        from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import Swal            from 'sweetalert2';

/**
 * Registration page component.
 * Collects name, email, and password, then calls the register action.
 */
export const RegisterPage = () => {
    const { register } = useContext(AuthContext);

    const [form, setForm] = useState({
        name:     '',
        email:    '',
        password: '',
    });

    /** Syncs text inputs with form state. */
    const onChange = ({ target }) => {
        const { name, value } = target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    /** Handles form submission — calls register and shows an error alert on failure. */
    const onSubmit = async (e) => {
        e.preventDefault();
        const result = await register(form.name, form.email, form.password);
        if (result !== true) {
            Swal.fire('Error', result, 'error');
        }
    };

    /** Returns true only when all three fields have content. */
    const isFormValid = () =>
        form.name.length > 0 &&
        form.email.length > 0 &&
        form.password.length > 0;

    return (
        <form
            className="login100-form validate-form flex-sb flex-w"
            onSubmit={onSubmit}
        >
            <span className="login100-form-title mb-3">Chat - Register</span>

            {/* Name field */}
            <div className="wrap-input100 validate-input mb-3">
                <input
                    className="input100"
                    type="text"
                    name="name"
                    placeholder="Name"
                    value={form.name}
                    onChange={onChange}
                />
                <span className="focus-input100" />
            </div>

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

            {/* Login link */}
            <div className="row mb-3">
                <div className="col text-right">
                    <Link to="/auth/login" className="txt1">
                        Already have an account?
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
                    Create account
                </button>
            </div>
        </form>
    );
};
