import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient, extractHalGames } from '../api/client';
import { Game, HalCollection } from '../types/api';

export const HomePage: React.FC = () => {
  const [games, setGames] = useState<Game[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    (async () => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<HalCollection<'games', Game>>('/games');
        if (isMounted) {
          const allGames = extractHalGames(response.data).filter(
            (g) => g.isActive !== false
          );
          setGames(allGames);
        }
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'Failed to load catalog');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    })();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <section>
      <div className="page-header">
        <h1 className="page-title">Minigame Catalog</h1>
        <p className="page-subtitle">
          Pick an arcade challenge, climb the leaderboards, and save your progress.
        </p>
      </div>

      {isLoading && (
        <div className="state-box" role="status">
          Loading minigames...
        </div>
      )}

      {!isLoading && error && (
        <div className="alert-error" role="alert">
          Unable to load game catalog: {error}
        </div>
      )}

      {!isLoading && !error && games.length === 0 && (
        <div className="state-box">No active minigames found in the catalog.</div>
      )}

      {!isLoading && !error && games.length > 0 && (
        <div className="catalog-grid">
          {games.map((game) => (
            <article key={game.id} className="game-card">
              <div className="game-thumb-wrap">
                <img
                  src={game.thumbnailUrl || `/minigames/${game.id}/thumbnail.png`}
                  alt={game.title}
                  className="game-thumb"
                />
                {game.category && (
                  <span className="game-category-tag">{game.category}</span>
                )}
              </div>
              <div className="game-card-body">
                <h2 className="game-card-title">{game.title}</h2>
                {game.description && (
                  <p className="game-card-desc">{game.description}</p>
                )}
                <Link
                  to={`/games/${encodeURIComponent(game.id)}`}
                  className="btn btn-primary"
                >
                  Play Now
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
};
