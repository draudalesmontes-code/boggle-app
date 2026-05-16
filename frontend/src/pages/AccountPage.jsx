import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AccountPage.css';

const STORAGE_KEY = 'bbUser';

const INFO_ROWS = [
    { label: 'Username', key: 'username', icon: '👤' },
    { label: 'Email',    key: 'email',    icon: '✉️'  },
    { label: 'Player ID', key: 'id',      icon: '🔑'  },
];

const AVATARS = [
    'avatar_bear.png',
    'avatar_blocks.png',
    'avatar_cube.png',
    'avatar_fox.png',
    'avatar_hourglass.png',
    'avatar_owl.png',
    'avatar_robot.png',
];

export default function AccountPage() {
    const navigate = useNavigate();

    const storedUser  = localStorage.getItem(STORAGE_KEY);
    const user        = storedUser ? JSON.parse(storedUser) : null;
    const playerName  = user?.username ?? 'Guest';

    const [selected, setSelected]   = useState(user?.profilePicture ?? null);
    const [saving, setSaving]       = useState(false);

    async function handleSelectAvatar(filename) {
        if (!user?.id || saving) return;
        setSaving(true);
        try {
            const res = await fetch(`/api/users/${user.id}/avatar`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ profilePicture: filename }),
            });
            if (!res.ok) return;
            const updated = await res.json();
            setSelected(updated.profilePicture);
            localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...user, profilePicture: updated.profilePicture }));
        } finally {
            setSaving(false);
        }
    }

    function handleSignOut() {
        localStorage.removeItem(STORAGE_KEY);
        navigate('/login');
    }

    return (
        <div className="account-page">
            <button className="account-back-btn" onClick={() => navigate('/home')}>
                ← Back
            </button>

            <div className="account-avatar">
                {selected
                    ? <img src={`/avatars/${selected}`} alt="avatar" className="account-avatar-img" />
                    : playerName.charAt(0).toUpperCase()
                }
            </div>
            <h1 className="account-username">{playerName}</h1>
            <p className="account-subtitle">Account Details</p>

            <div className="account-card">
                {INFO_ROWS.map(({ label, key, icon }) => (
                    <div key={key} className="account-row">
                        <span className="account-row-icon">{icon}</span>
                        <div className="account-row-text">
                            <span className="account-row-label">{label}</span>
                            <span className="account-row-value">{user?.[key] ?? '—'}</span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="avatar-picker">
                <p className="avatar-picker-label">Choose Profile Picture</p>
                <div className="avatar-picker-grid">
                    {AVATARS.map(filename => (
                        <button
                            key={filename}
                            className={`avatar-option${selected === filename ? ' avatar-option--active' : ''}`}
                            onClick={() => handleSelectAvatar(filename)}
                            disabled={saving}
                        >
                            <img src={`/avatars/${filename}`} alt={filename} />
                        </button>
                    ))}
                </div>
            </div>

            <button className="account-signout-btn" onClick={handleSignOut}>
                Sign Out
            </button>
        </div>
    );
}
