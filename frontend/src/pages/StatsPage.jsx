import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import './StatsPage.css';

const STORAGE_KEY = 'bbUser';

const STAT_CARDS = [
    { key: 'gamesPlayed', label: 'Games Played', icon: '🎮' },
    { key: 'gamesWon',    label: 'Games Won',    icon: '🏆' },
    { key: 'wordsFound',  label: 'Words Found',  icon: '📝' },
];

export default function StatsPage() {
    const location  = useLocation();
    const navigate  = useNavigate();

    const storedUser = localStorage.getItem(STORAGE_KEY);
    const user       = storedUser ? JSON.parse(storedUser) : null;
    const playerName = location.state?.playerName ?? user?.username ?? 'Guest';
    const userId     = location.state?.userId     ?? user?.id       ?? null;

    const [stats, setStats]   = useState({ gamesWon: 0, wordsFound: 0, totalPoints: 0 });
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        if (!userId) return;

        fetch(`/api/users/${userId}/stats`)
            .then(res => { if (!res.ok) throw new Error(); return res.json(); })
            .then(data => { setStats(data); setLoaded(true); })
            .catch(() => {});
    }, [userId]);

    return (
        <div className="stats-page">
            <button className="stats-back-btn" onClick={() => navigate('/home')}>
                ← Back
            </button>

            <div className="stats-avatar">{playerName.charAt(0).toUpperCase()}</div>
            <h1 className="stats-username">{playerName}</h1>
            <p className="stats-subtitle">Lifetime Performance</p>

            <div className="stats-grid">
                {STAT_CARDS.map(({ key, label, icon }) => (
                    <div key={key} className={`stat-card${loaded ? ' stat-card--loaded' : ''}`}>
                        <span className="stat-icon">{icon}</span>
                        <span className="stat-value">{stats[key]}</span>
                        <span className="stat-label">{label}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}
