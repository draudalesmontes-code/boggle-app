import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';
import { clearExpiredSession } from '../utils/session';

const STORAGE_KEY = 'bbUser';

/**
 * Display home Page with options to play solo, view rank, and view account. Only play solo is implemented for now.
 */
export default function HomePage() {
    const storedUser  = localStorage.getItem(STORAGE_KEY);
    const user        = storedUser ? JSON.parse(storedUser) : null;
    const playerName  = user?.username ?? 'Guest';

    const [loading, setLoading] = useState(false);
    const [showMultiMenu, setShowMultiMenu] = useState(false);
    const [multiplayerGameCode, setMultiplayerGameCode] = useState('');
    const [waitingGames, setWaitingGames] = useState([]);
    const [waitingGamesLoading, setWaitingGamesLoading] = useState(false);
    const navigate = useNavigate();

    /**
     * Fetches the list of open (WAITING) multiplayer games whenever the multiplayer menu is opened.
     */
    useEffect(() => {
        if (!showMultiMenu) return;

        async function fetchWaitingGames() {
            setWaitingGamesLoading(true);
            try {
                const res = await fetch('/api/game/list-waiting');
                if (res.ok) {
                    const data = await res.json();
                    // Filter out games created by the current user
                    setWaitingGames(data.filter(g => g.player1Id !== user?.id));
                }
            } catch {
                // Silently fail — the list just won't populate
            } finally {
                setWaitingGamesLoading(false);
            }
        }

        fetchWaitingGames();
    }, [showMultiMenu]);

    /**
     * Creates a game via POST /api/game, then navigates to GamePage.
     * GamePage will fetch the board itself using the returned gameId.
     *
     * @param mode String indicating desired game mode, e.g. SOLO, MULTIPLAYER
     */
    async function handleCreateGame(mode) {
        if (!user?.id) {
            alert('You must be logged in to start a game.');
            return;
        }

        setLoading(true);
        try {
            const res = await fetch('/api/game', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ mode: mode, playerId: user.id }),
            });

            if (!res.ok) {
                if (res.status === 404 && user?.isGuest) {
                    clearExpiredSession(navigate);
                    return;
                }
                alert('Failed to create game. Please try again.');
                return;
            }

            const data = await res.json();
            navigate('/game', {
                state: {
                    playerName,
                    gameId:   data.gameId,
                    playerId: user.id,
                },
            });
        } catch {
            alert('Network error. Is the server running?');
        } finally {
            setLoading(false);
        }
    }

    async function handleJoinMultiplayerGame(e, gameIdOverride) {
        if (e) e.preventDefault();
        if (!user?.id) {
            alert('You must be logged in to start a game.');
            return;
        }

        const gameId = gameIdOverride ?? multiplayerGameCode;

        setLoading(true);
        try {
            const res = await fetch('/api/game/' + gameId + '/join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ playerId: user.id }),
            });

            if (!res.ok) {
                if (res.status === 404 && user?.isGuest) {
                    clearExpiredSession(navigate);
                    return;
                }
                alert('Failed to join game. Check the game code and try again.');
                return;
            }

            const data = await res.json();
            navigate('/game', {
                state: {
                    playerName,
                    gameId:        data.gameId,
                    playerId:      user.id,
                    opponentId:    data.player1Id,
                    opponentName:  data.player1Username,
                },
            });
        } catch {
            alert('Network error. Is the server running?');
        } finally {
            setLoading(false);
        }
    }

    const handleChangeMultiplayerGameCode = (event) => {
        setMultiplayerGameCode(event.target.value);
    };

    return (
        <div className="home-page">
            <div className="home-avatar">
                {user?.profilePicture
                    ? <img src={`/avatars/${user.profilePicture}`} alt="avatar" className="home-avatar-img" />
                    : playerName.charAt(0).toUpperCase()
                }
            </div>
            <h1 className="home-username">{playerName}</h1>

            <div className="home-buttons">
                <button
                    className="home-btn home-btn--primary"
                    onClick={() => handleCreateGame("SOLO")}
                    disabled={loading}
                >
                    {loading ? 'Starting…' : '▶ Play Solo'}
                </button>
                <button
                    className="home-btn home-btn--primary"
                    onClick={() => setShowMultiMenu(true)}
                    disabled={loading}
                >
                    {loading ? 'Starting…' : '▶ Play Multiplayer'}
                </button>

                <button
                    className="home-btn home-btn--primary"
                    onClick={() => navigate('/stats', { state: { playerName, userId: user?.id } })}
                    disabled={loading}
                >
                    🏆 Stats
                </button>

                <button
                    className="home-btn home-btn--primary"
                    onClick={() => navigate('/account', { state: { playerName } })}
                    disabled={loading}
                >
                    👤 My Account
                </button>

                <button
                    className="home-btn home-btn--secondary"
                    onClick={() => navigate('/how-to-play')}
                    disabled={loading}
                >
                    ? How to Play
                </button>
            </div>

            {/* Multiplayer menu for creating and joining games */}
            {showMultiMenu && <div className="multiplayer-menu">
                <button
                    className="home-btn home-btn--close"
                    onClick={() => setShowMultiMenu(false)}
                    disabled={loading}
                >
                    {/* SVG vector graphics close icon */}
                    <svg aria-hidden="true" viewBox="0 0 24 24" style={{ width: '24px', height: '24px', fill: 'currentColor' }}>
                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
                    </svg>
                </button>

                <button
                    className="home-btn home-btn--primary"
                    onClick={() => handleCreateGame("MULTIPLAYER")}
                    disabled={loading}
                >
                    {loading ? 'Starting…' : '▶ Create Match'}
                </button>

                {/* Open games browser */}
                <div className="waiting-games">
                    <h2 className="waiting-games__title">Open Games</h2>
                    {waitingGamesLoading && <p className="waiting-games__status">Loading…</p>}
                    {!waitingGamesLoading && waitingGames.length === 0 && (
                        <p className="waiting-games__status">No open games right now.</p>
                    )}
                    {!waitingGamesLoading && waitingGames.length > 0 && (
                        <ul className="waiting-games__list">
                            {waitingGames.map(game => (
                                <li key={game.gameId} className="waiting-games__item">
                                    <span className="waiting-games__info">
                                        <span className="waiting-games__player">{game.player1Username}</span>
                                        <span className="waiting-games__id">#{game.gameId}</span>
                                    </span>
                                    <button
                                        className="home-btn home-btn--primary waiting-games__join-btn"
                                        onClick={(e) => handleJoinMultiplayerGame(e, game.gameId)}
                                        disabled={loading}
                                    >
                                        Join
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Join by session code form */}
                <form onSubmit={handleJoinMultiplayerGame}>
                    {/* Session code text input field */}
                    <label>
                        Join with a code:
                        <input
                            type="text"
                            value={multiplayerGameCode}
                            onChange={handleChangeMultiplayerGameCode}
                        />
                    </label>

                    {/* Joins game with game code entered in field */}
                    <button
                        className="home-btn home-btn--primary"
                        type="submit"
                        disabled={loading}
                    >
                        {loading ? 'Starting…' : '▶ Join Match'}
                    </button>
                </form>
            </div> /* End multiplayer menu */}
        </div>
    );
}