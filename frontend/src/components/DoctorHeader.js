'use client';
import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useSettings } from '../context/SettingsContext';
import { AuthService } from '../services/api';
import Link from 'next/link';

export default function DoctorHeader() {
  const router = useRouter();
  const { t } = useSettings();
  const [userProfile, setUserProfile] = useState(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [theme, setTheme] = useState('dark');
  const dropdownRef = useRef(null);
  const notifRef = useRef(null);

  useEffect(() => {
    // Tema yükle
    const savedTheme = localStorage.getItem('theme') || 'dark';
    setTheme(savedTheme);
    document.documentElement.setAttribute('data-theme', savedTheme);

    const fetchProfile = async () => {
      try {
        const data = await AuthService.getMe();
        setUserProfile(data);
        if (data && data.id) {
          const notifs = await import('../services/api').then(m => m.NotificationService.getByPatient(data.id));
          setNotifications(notifs.slice(0, 5));
          setUnreadCount(notifs.filter(n => !n.read).length);
        }
      } catch (error) {
        console.error("Profil bilgisi alınamadı:", error);
      }
    };
    fetchProfile();

    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
      if (notifRef.current && !notifRef.current.contains(event.target)) {
        setIsNotifOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/login');
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  const initial = userProfile?.firstName && userProfile?.lastName 
    ? `${userProfile.firstName.charAt(0)}${userProfile.lastName.charAt(0)}`.toUpperCase() 
    : userProfile?.firstName ? userProfile.firstName.charAt(0).toUpperCase() : '';

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '1rem 2rem',
    backgroundColor: 'var(--surface)',
    borderBottom: '1px solid var(--border)',
    boxShadow: 'var(--shadow-sm)'
  };

  const logoStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    cursor: 'pointer'
  };

  const logoIconStyle = {
    width: '38px',
    height: '38px',
    minWidth: '38px',
    background: 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
    borderRadius: '10px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: 'white'
  };

  const titleStyle = {
    fontSize: '1.25rem',
    fontWeight: '700',
    color: 'var(--text-main)',
    letterSpacing: '-0.025em'
  };

  const subtitleStyle = {
    fontSize: '0.75rem',
    color: 'var(--text-muted)',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
    marginTop: '2px'
  };

  const rightActionsStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '1.5rem'
  };

  const iconButtonStyle = {
    background: 'none',
    border: 'none',
    color: 'var(--text-muted)',
    cursor: 'pointer',
    padding: '8px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.2s',
  };

  const dropdownMenuStyle = {
    position: 'absolute',
    top: 'calc(100% + 10px)',
    right: '0',
    width: '260px',
    backgroundColor: 'var(--surface)',
    border: '1px solid var(--border)',
    borderRadius: '16px',
    boxShadow: '0 10px 40px -10px rgba(0,0,0,0.2)',
    zIndex: 100,
    overflow: 'hidden'
  };

  const dropdownHeaderStyle = {
    padding: '1.2rem',
    background: 'linear-gradient(135deg, rgba(var(--primary-rgb), 0.1), transparent)',
    borderBottom: '1px solid var(--border)',
    display: 'flex',
    flexDirection: 'column'
  };

  const dropdownBodyStyle = {
    padding: '1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.8rem'
  };

  const dropdownItemStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '0.85rem'
  };

  const itemLabelStyle = {
    color: 'var(--text-muted)',
    fontWeight: '500'
  };

  const itemValueStyle = {
    color: 'var(--text-main)',
    fontWeight: '600'
  };

  const dropdownFooterStyle = {
    padding: '1rem',
    borderTop: '1px solid var(--border)',
    backgroundColor: 'rgba(var(--background-rgb), 0.5)'
  };

  const logoutBtnStyle = {
    width: '100%',
    padding: '0.8rem',
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    color: '#ef4444',
    border: '1px solid rgba(239, 68, 68, 0.2)',
    borderRadius: '8px',
    fontWeight: '600',
    textAlign: 'center',
    cursor: 'pointer',
    transition: 'all 0.2s ease'
  };

  return (
    <header style={headerStyle}>
      {/* Sol Logo */}
      <div style={logoStyle} onClick={() => router.push('/')}>
        <div style={logoIconStyle}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
              <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
            </svg>
        </div>
        <div>
          <div style={titleStyle}>{t('logo_title')}</div>
          <div style={subtitleStyle}>{t('logo_desc')}</div>
        </div>
      </div>

      {/* Sağ Aksiyonlar */}
      <div style={rightActionsStyle}>

        <button 
          onClick={toggleTheme} 
          style={iconButtonStyle}
          onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--background)'; e.currentTarget.style.color = 'var(--text-main)'; }}
          onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)'; }}
          title={theme === 'light' ? t('dark_mode') : t('light_mode')}
        >
          {theme === 'light' ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>
          ) : (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="4.22" x2="19.78" y2="5.64"></line></svg>
          )}
        </button>

        <button 
          onClick={() => router.push('/settings')} 
          style={iconButtonStyle}
          onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--background)'; e.currentTarget.style.color = 'var(--text-main)'; }}
          onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)'; }}
          title={t('settings')}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
        </button>

        <div style={{ position: 'relative' }} ref={notifRef}>
          <button 
            onClick={() => setIsNotifOpen(!isNotifOpen)} 
            style={iconButtonStyle}
            onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--background)'; e.currentTarget.style.color = 'var(--text-main)'; }}
            onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)'; }}
            title={t('notifications')}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
            {unreadCount > 0 && (
              <span style={{ position: 'absolute', top: '2px', right: '2px', width: '8px', height: '8px', backgroundColor: '#ef4444', borderRadius: '50%' }}></span>
            )}
          </button>
          
          {isNotifOpen && (
            <div style={{...dropdownMenuStyle, right: '-50px', width: '320px'}}>
              <div style={dropdownHeaderStyle}>
                <strong style={{ color: 'var(--text-main)', fontSize: '1.05rem', display: 'block' }}>{t('notifications')}</strong>
              </div>
              <div style={{...dropdownBodyStyle, maxHeight: '300px', overflowY: 'auto', padding: '0'}}>
                {notifications.length === 0 ? (
                  <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>{t('no_notifications')}</div>
                ) : (
                  notifications.map(notif => (
                    <div 
                      key={notif.id} 
                      style={{ 
                        padding: '1rem', 
                        borderBottom: '1px solid var(--border)',
                        backgroundColor: notif.read ? 'transparent' : 'rgba(16, 185, 129, 0.05)',
                        cursor: 'pointer'
                      }}
                      onClick={() => {
                        setIsNotifOpen(false);
                        router.push('/patient-notifications');
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                        {!notif.read && <div style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#ef4444' }}></div>}
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {new Date(notif.createdAt).toLocaleString('tr-TR', { dateStyle: 'short', timeStyle: 'short' })}
                        </span>
                      </div>
                      <p style={{ color: notif.read ? 'var(--text-muted)' : 'var(--text-main)', fontSize: '0.85rem', margin: 0, fontWeight: notif.read ? '400' : '500', lineHeight: '1.4' }}>
                        {notif.message}
                      </p>
                    </div>
                  ))
                )}
              </div>
              <div style={dropdownFooterStyle}>
                <button 
                  onClick={() => { setIsNotifOpen(false); router.push('/patient-notifications'); }} 
                  style={{...logoutBtnStyle, backgroundColor: 'transparent', color: 'var(--primary)', border: 'none', textAlign: 'center', width: '100%', cursor: 'pointer', fontWeight: '600'}}
                  onMouseOver={(e) => { e.currentTarget.style.textDecoration = 'underline'; }}
                  onMouseOut={(e) => { e.currentTarget.style.textDecoration = 'none'; }}
                >
                  {t('see_all_notifications')}
                </button>
              </div>
            </div>
          )}
        </div>

        <div style={{ position: 'relative' }} ref={dropdownRef}>
          <button 
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer', padding: '0.5rem', borderRadius: '8px', background: 'transparent', border: 'none' }}
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
          >
            <div style={{ width: '36px', height: '36px', borderRadius: '50%', backgroundColor: 'var(--primary)', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '600', fontSize: '1rem' }}>{initial}</div>
            <div style={{ display: 'flex', flexDirection: 'column', textAlign: 'left' }}>
              <span style={{ fontSize: '0.9rem', fontWeight: '600', color: 'var(--text-main)', lineHeight: '1.2' }}>Dr. {userProfile?.firstName || ''} {userProfile?.lastName || ''}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', lineHeight: '1.2' }}>{t('doctor')}</span>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--text-muted)' }}>
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>

          {isDropdownOpen && (
            <div style={dropdownMenuStyle}>
              <div style={dropdownHeaderStyle}>
                <strong style={{ color: 'var(--text-main)', fontSize: '1.05rem', marginBottom: '0.2rem', display: 'block' }}>
                  {userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : t('unknown_patient')}
                </strong>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  {userProfile?.email || '-'}
                </span>
              </div>
              <div style={dropdownBodyStyle}>
                <div style={dropdownItemStyle}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    <span style={itemLabelStyle}>{t('role')}</span>
                  </div>
                  <span style={{ ...itemValueStyle, color: 'var(--primary)', fontWeight: '700' }}>{t('doctor')}</span>
                </div>
                <div style={dropdownItemStyle}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    <span style={itemLabelStyle}>{t('tc_no')}</span>
                  </div>
                  <span style={itemValueStyle}>{userProfile.username}</span>
                </div>
                <div style={dropdownItemStyle}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                    <span style={itemLabelStyle}>{t('phone')}</span>
                  </div>
                  <span style={itemValueStyle}>{userProfile.phoneNumber || '-'}</span>
                </div>
              </div>
              <div style={dropdownFooterStyle}>
                <button 
                  onClick={handleLogout} 
                  style={logoutBtnStyle}
                  onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#ef4444'; e.currentTarget.style.color = 'white'; }}
                  onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'; e.currentTarget.style.color = '#ef4444'; }}
                >
                  {t('logout')}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
