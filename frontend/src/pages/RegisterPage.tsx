import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await register(username.trim(), email.trim(), password);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="panel-card">
        <h1 className="page-title">Create Account</h1>
        <p className="page-subtitle">
          Join FrivLegion to compete on leaderboards and save your game state.
        </p>

        <form className="form-stack" onSubmit={handleSubmit} noValidate>
          {error && (
            <div className="alert-error" role="alert">
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="register-username" className="form-label">
              Username
            </label>
            <input
              id="register-username"
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="arcade_hero"
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="register-email" className="form-label">
              Email
            </label>
            <input
              id="register-email"
              type="email"
              className="form-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="player@example.com"
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="register-password" className="form-label">
              Password (8+ characters)
            </label>
            <input
              id="register-password"
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minimum 8 characters"
              autoComplete="new-password"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Creating Account...' : 'Create Account'}
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
          Already registered?{' '}
          <Link
            to="/login"
            style={{ color: 'var(--neon-cyan)', fontWeight: 700 }}
          >
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
};
