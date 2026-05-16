import { useState } from 'react';
import './WordsModal.css';

export default function WordsModal({
  comparison,
  isMultiplayer = false,
  mySortedWords = [],
  opponentSortedWords = [],
  score = 0,
  isGameOver = false,
  onClose = null,
  onHome = null,
  onPlayAgain = null,
}) {
  const [activeTab, setActiveTab] = useState('comparison');

  return (
    <div className="modal-overlay">
      <div className="modal-box">

        <div id="header-style">
          <h1>Game Over</h1>
          {isGameOver && (
            <p className="modal-score-line">Final Score: <strong>{score}</strong></p>
          )}
        </div>

        <div className="modal-tabs">
          <button
            className={`modal-tab${activeTab === 'comparison' ? ' modal-tab--active' : ''}`}
            onClick={() => setActiveTab('comparison')}
          >
            Comparison
          </button>
          {isMultiplayer && (
            <button
              className={`modal-tab${activeTab === 'vs-opponent' ? ' modal-tab--active' : ''}`}
              onClick={() => setActiveTab('vs-opponent')}
            >
              vs Opponent
            </button>
          )}
        </div>

        {activeTab === 'comparison' && (
          comparison ? (
            <div className="modal-comparison">
              <div className="modal-comparison-col">
                <h3 className="modal-comparison-title modal-comparison-title--found">
                  Found ({comparison.foundWords.length})
                </h3>
                <ul>
                  {comparison.foundWords.map((w, i) => (
                    <li key={i} className="modal-comparison-item--found">
                      <span className="modal-word-text">{w.word}</span>
                      <span className="modal-word-pts">+{w.points}</span>
                    </li>
                  ))}
                </ul>
              </div>
              <div className="modal-comparison-col">
                <h3 className="modal-comparison-title modal-comparison-title--missed">
                  Missed ({comparison.missedWords.length})
                </h3>
                <ul>
                  {comparison.missedWords.map((w, i) => (
                    <li key={i} className="modal-comparison-item--missed">
                      <span className="modal-word-text">{w.word}</span>
                      <span className="modal-word-pts">+{w.points}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ) : (
            <p className="modal-empty">Loading&hellip;</p>
          )
        )}

        {activeTab === 'vs-opponent' && isMultiplayer && (
          <div className="modal-comparison">
            <div className="modal-comparison-col">
              <h3 className="modal-comparison-title modal-comparison-title--found">
                You ({mySortedWords.length})
              </h3>
              <ul>
                {mySortedWords.map((fw, i) => (
                  <li key={i} className="modal-comparison-item--found">
                    <span className="modal-word-text">{fw.word}</span>
                    <span className="modal-word-pts">+{fw.points}</span>
                  </li>
                ))}
              </ul>
            </div>
            <div className="modal-comparison-col">
              <h3 className="modal-comparison-title modal-comparison-title--missed">
                Opponent ({opponentSortedWords.length})
              </h3>
              <ul>
                {opponentSortedWords.map((fw, i) => (
                  <li key={i} className="modal-comparison-item--missed">
                    <span className="modal-word-text">{fw.word}</span>
                    <span className="modal-word-pts">+{fw.points}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {isGameOver ? (
          <div className="modal-footer">
            <button className="modal-footer-btn modal-footer-btn--home" onClick={onHome}>
              Home
            </button>
            <button className="modal-footer-btn modal-footer-btn--play-again" onClick={onPlayAgain}>
              {isMultiplayer ? 'New Match' : 'Play Again'}
            </button>
          </div>
        ) : (
          <button className="modal-close-btn" onClick={onClose}>
            Close
          </button>
        )}

      </div>
    </div>
  );
}
