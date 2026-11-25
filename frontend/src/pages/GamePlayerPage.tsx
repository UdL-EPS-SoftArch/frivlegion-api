import React from 'react';
import { Link, useParams } from 'react-router-dom';

export const GamePlayerPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  return (
    <section>
      <div
        className="page-header"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '16px',
        }}
      >
        <div>
          <h1 className="page-title">Playing: {id}</h1>
          <p className="page-subtitle">
            Iframe game player container shell (Issue 4.1).
          </p>
        </div>
        <Link to="/" className="btn btn-outline">
          Back to Catalog
        </Link>
      </div>

      <div
        className="panel-card"
        style={{
          padding: '12px',
          display: 'flex',
          justifyContent: 'center',
          background: '#050811',
        }}
      >
        {id ? (
          <iframe
            title={`Minigame ${id}`}
            src={`/minigames/${encodeURIComponent(id)}/index.html`}
            style={{
              width: '100%',
              height: '620px',
              border: 'none',
              borderRadius: '12px',
            }}
          />
        ) : (
          <div className="state-box">No game ID specified.</div>
        )}
      </div>
    </section>
  );
};
