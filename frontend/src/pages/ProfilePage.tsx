import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProfilePage: React.FC = () => {
  const { user, roles, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="state-box" role="status">
        Loading your profile...
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <section>
      <div className="page-header">
        <h1 className="page-title">Player Profile</h1>
        <p className="page-subtitle">
          Authenticated identity information from <code>GET /identity</code>.
        </p>
      </div>

      <div className="panel-card">
        <div className="profile-grid">
          <div className="profile-item">
            <div className="profile-item-label">Username</div>
            <div className="profile-item-value">{user.username}</div>
          </div>

          <div className="profile-item">
            <div className="profile-item-label">Email</div>
            <div className="profile-item-value">{user.email}</div>
          </div>

          <div className="profile-item">
            <div className="profile-item-label">Roles</div>
            <div className="profile-item-value">
              {roles.length > 0 ? roles.join(', ') : 'ROLE_USER'}
            </div>
          </div>

          <div className="profile-item">
            <div className="profile-item-label">Member Since</div>
            <div className="profile-item-value">
              {user.createdAt
                ? new Date(user.createdAt).toLocaleDateString()
                : 'Active'}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
