import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await login(username.trim(), password);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="panel-card">
        <h1 className="page-title">Player Sign In</h1>
        <p className="page-subtitle">
          Enter your username or email to access your saved games and scores.
        </p>

        <form className="form-stack" onSubmit={handleSubmit} noValidate>
          {error && (
            <div className="alert-error" role="alert">
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="login-username" className="form-label">
              Username or Email
            </label>
            <input
              id="login-username"
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="player1"
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="login-password" className="form-label">
              Password
            </label>
            <input
              id="login-password"
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              autoComplete="current-password"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Signing In...' : 'Sign In'}
          </button>
        </form>

        <p
          style={{
            marginTop: '18px',
            fontSize: '0.88rem',
            color: 'var(--text-secondary)',
            textAlign: 'center',
          }}
        >
          Need an account?{' '}
          <Link
            to="/register"
            style={{ color: 'var(--neon-cyan)', fontWeight: 700 }}
          >
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
};
