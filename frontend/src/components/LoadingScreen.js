import { useState, useEffect } from 'react';
import { useSettings } from '../context/SettingsContext';

export default function LoadingScreen({ fullScreen = false }) {
  const settings = useSettings();
  const t = settings?.t || ((key) => key);
  const [showWarning, setShowWarning] = useState(false);

  useEffect(() => {
    // Show the cold-start warning after 4 seconds of loading
    const timer = setTimeout(() => {
      setShowWarning(true);
    }, 4000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      alignItems: 'center', 
      justifyContent: 'center', 
      padding: fullScreen ? '0' : '4rem 2rem', 
      minHeight: fullScreen ? '100vh' : 'auto',
      textAlign: 'center',
      backgroundColor: fullScreen ? 'var(--background)' : 'transparent'
    }}>
      <div style={{
        width: '50px',
        height: '50px',
        border: '4px solid rgba(var(--primary-rgb), 0.2)',
        borderTopColor: 'var(--primary)',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite',
        marginBottom: '1.5rem'
      }}></div>
      
      <h3 style={{ color: 'var(--text-main)', marginBottom: '0.5rem', fontWeight: '600' }}>{t('loading')}</h3>
      
      {showWarning && (
        <div style={{ 
          marginTop: '1.5rem', 
          padding: '1.2rem', 
          backgroundColor: 'rgba(245, 158, 11, 0.1)', 
          border: '1px solid rgba(245, 158, 11, 0.3)', 
          borderRadius: '12px',
          color: '#d97706',
          maxWidth: '500px',
          fontSize: '0.95rem',
          lineHeight: '1.5',
          animation: 'fadeIn 0.5s ease-in-out',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)'
        }}>
          <strong style={{ display: 'block', marginBottom: '0.5rem', fontSize: '1.05rem' }}>{t('backend_waking_up')}</strong>
          <span style={{ opacity: 0.9 }}>{t('backend_waking_up_desc')}</span>
        </div>
      )}
      
      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(-10px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
}
