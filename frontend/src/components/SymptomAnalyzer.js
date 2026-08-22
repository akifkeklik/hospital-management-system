'use client';
import { useState } from 'react';
import { AiService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import styles from './SymptomAnalyzer.module.css';

/**
 * 🤖 AI Semptom Analiz Bileşeni
 * 
 * Hasta şikayetlerini yazar → Gemini AI analiz eder → Bölüm önerisi döner.
 * Randevu alma akışına entegre edilir (book-appointment sayfasında).
 */
export default function SymptomAnalyzer({ departments, onDepartmentSelect, onSkip }) {
  const { t } = useSettings();
  const [symptoms, setSymptoms] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleAnalyze = async () => {
    if (!symptoms.trim() || symptoms.trim().length < 10) {
      setError(t('ai_min_chars'));
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);

    try {
      const deptNames = departments.map(d => d.name);
      const response = await AiService.analyzeSymptoms(symptoms.trim(), deptNames);
      
      if (response && response.suggestedDepartment) {
        setResult(response);
      } else {
        setError(t('ai_error_generic'));
      }
    } catch (err) {
      console.error('AI analiz hatası:', err);
      setError(t('ai_error_generic'));
    } finally {
      setLoading(false);
    }
  };

  const handleBookFromResult = () => {
    if (!result) return;
    const matchedDept = departments.find(
      d => d.name.toLowerCase() === result.suggestedDepartment.toLowerCase()
    );
    if (matchedDept) {
      onDepartmentSelect(matchedDept);
    }
  };

  const handleRetry = () => {
    setResult(null);
    setError('');
    setSymptoms('');
  };

  return (
    <div className={styles.container}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.aiIcon}>🧠</div>
        <div className={styles.headerText}>
          <h3>
            {t('ai_title')}
            <span className={styles.badge}>
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
              </svg>
              AI
            </span>
          </h3>
          <p>{t('ai_subtitle')}</p>
        </div>
      </div>

      {/* Loading State */}
      {loading && (
        <div className={styles.loadingContainer}>
          <div className={styles.aiPulse}>🧠</div>
          <p className={styles.loadingText}>
            {t('ai_analyzing')}<span className={styles.loadingDots}></span>
          </p>
        </div>
      )}

      {/* Input Area — gizlenir eğer sonuç varsa */}
      {!loading && !result && (
        <div className={styles.inputArea}>
          <textarea
            className={styles.textarea}
            placeholder={t('ai_placeholder')}
            value={symptoms}
            onChange={(e) => {
              if (e.target.value.length <= 1000) {
                setSymptoms(e.target.value);
                setError('');
              }
            }}
            rows={4}
          />
          <div className={styles.inputFooter}>
            <span className={styles.charCount}>{symptoms.length}/1000</span>
            <button 
              className={styles.analyzeBtn} 
              onClick={handleAnalyze}
              disabled={symptoms.trim().length < 10}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              {t('ai_analyze_btn')}
            </button>
          </div>
          {error && (
            <div className={styles.errorMsg}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {error}
            </div>
          )}
        </div>
      )}

      {/* Result Card */}
      {!loading && result && (
        <div className={styles.resultCard}>
          <div className={styles.resultHeader}>
            <div>
              <div className={styles.resultLabel}>{t('ai_suggested_dept')}</div>
              <div className={styles.resultDept}>{result.suggestedDepartment}</div>
            </div>
            <div className={styles.confidenceContainer}>
              <span className={styles.confidenceLabel}>{t('ai_confidence')}</span>
              <div className={styles.confidenceBar}>
                <div 
                  className={styles.confidenceFill} 
                  style={{ width: `${result.confidencePercent}%` }}
                />
              </div>
              <span className={styles.confidenceValue}>%{result.confidencePercent}</span>
            </div>
          </div>

          <div className={styles.resultBody}>
            <p className={styles.explanation}>{result.explanation}</p>
            {result.disclaimer && (
              <div className={styles.disclaimer}>{result.disclaimer}</div>
            )}
          </div>

          <div className={styles.resultActions}>
            <button className={styles.bookBtn} onClick={handleBookFromResult}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              {t('ai_book_this_dept')}
            </button>
            <button className={styles.retryBtn} onClick={handleRetry}>
              {t('ai_retry')}
            </button>
          </div>
        </div>
      )}

      {/* Skip Link */}
      <div className={styles.skipLink}>
        <button className={styles.skipBtn} onClick={onSkip}>
          {t('ai_skip')} →
        </button>
      </div>
    </div>
  );
}
