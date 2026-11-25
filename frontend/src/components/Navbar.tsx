import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand-link">
          <span>FrivLegion</span>
          <span className="brand-badge">Arcade</span>
        </Link>

        <nav className="nav-links" aria-label="Main navigation">
          <NavLink
            to="/"
            end
            className={({ isActive }) =>
              `nav-item${isActive ? ' active' : ''}`
            }
          >
            Catalog
          </NavLink>

          {isAuthenticated && user ? (
            <>
              <NavLink
                to="/profile"
                className={({ isActive }) =>
                  `nav-item${isActive ? ' active' : ''}`
                }
              >
                Profile
              </NavLink>
              <span className="user-pill" data-testid="navbar-username">
                <span>{user.username}</span>
                {isAdmin && <span className="role-chip">Admin</span>}
              </span>
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleLogout}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink
                to="/login"
                className={({ isActive }) =>
                  `nav-item${isActive ? ' active' : ''}`
                }
              >
                Login
              </NavLink>
              <Link to="/register" className="btn btn-primary">
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};
