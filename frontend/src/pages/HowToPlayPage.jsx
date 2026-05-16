import { useNavigate } from 'react-router-dom';
import './HowToPlayPage.css';

const RULES = [
    {
        heading: 'Build words on the board',
        body: 'Drag across adjacent letter tiles to form a word. Tiles connect horizontally, vertically, or diagonally.',
    },
    {
        heading: 'Each tile can only be used once',
        body: 'You cannot reuse the same tile within a single word. Backtrack along your path to undo the last tile.',
    },
    {
        heading: 'Minimum 3 letters',
        body: 'Words shorter than 3 letters are not accepted.',
    },
    {
        heading: 'Words must be in the dictionary',
        body: 'Only real English words score points. Proper nouns and abbreviations are not accepted.',
    },
    {
        heading: 'No duplicate words',
        body: 'Each word can only be submitted once per game session.',
    },
    {
        heading: 'Scoring by word length',
        body: '3–4 letters = 1 pt · 5 letters = 2 pts · 6 letters = 3 pts · 7 letters = 5 pts · 8+ letters = 11 pts',
    },
];

/**
 * Standalone page explaining the rules of Boggle.
 * Navigated to from HomePage via the How to Play button.
 */
export default function HowToPlayPage() {
    const navigate = useNavigate();

    return (
        <div className="htp-page">
            <div className="htp-card">
                <button className="htp-back" onClick={() => navigate('/home')}>
                    ← Back
                </button>
                <h1 className="htp-title">How to Play</h1>
                <p className="htp-subtitle">Boggle Buddies — Quick Rules</p>
                <ol className="htp-rules">
                    {RULES.map((rule, i) => (
                        <li key={i} className="htp-rule">
                            <span className="htp-rule-heading">{rule.heading}</span>
                            <span className="htp-rule-body">{rule.body}</span>
                        </li>
                    ))}
                </ol>
            </div>
        </div>
    );
}
