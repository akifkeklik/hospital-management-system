'use client';
import { useEffect, useState } from 'react';
import { NotificationService, AuthService } from '../../services/api';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from '../shared.module.css';

export default function PatientNotificationsPage() {
  const { t } = useSettings();
  const [notifications, setNotifications] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [userRole, setUserRole] = useState(null);
  const [broadcastMessage, setBroadcastMessage] = useState('');
  const [broadcasting, setBroadcasting] = useState(false);

  useEffect(() => {
    setMounted(true);
    checkAuthAndFetch();
  }, []);

  const checkAuthAndFetch = async () => {
    setLoading(true);
    try {
      const me = await AuthService.getMe();
      if (me) {
        setUserRole(me.role);
        if (me.role === 'PATIENT' || me.role === 'ROLE_PATIENT' || me.role === 'HASTA' || me.role === 'ROLE_HASTA') {
          const myNotifications = await NotificationService.getByPatient(me.id);
          setNotifications(myNotifications);
        } else if (me.role === 'DOCTOR' || me.role === 'ROLE_DOCTOR' || me.role === 'HEKIM' || me.role === 'ROLE_HEKIM') {
          const myNotifications = await NotificationService.getByDoctor(me.id);
          setNotifications(myNotifications);
        }
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await NotificationService.markAsRead(id);
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n));
    } catch (err) {
      console.error('Hata:', err);
    }
  };

  const handleBroadcast = async (e) => {
    e.preventDefault();
    if (!broadcastMessage.trim()) return;
    setBroadcasting(true);
    try {
      await NotificationService.broadcastToDoctors(broadcastMessage);
      setBroadcastMessage('');
      toast.success(t('Duyuru tüm hekimlere başarıyla gönderildi!'));
    } catch (err) {
      console.error(err);
      toast.error(t('Duyuru gönderilemedi.'));
    } finally {
      setBroadcasting(false);
    }
  };

  if (!mounted) return null;

  return (
    <div style={{ padding: '1.5rem 3rem', maxWidth: '1200px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
        <div style={{ padding: '0.75rem', backgroundColor: 'var(--primary)', borderRadius: '12px', color: 'white', display: 'flex' }}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        </div>
        <div>
          <h1 style={{ fontSize: '1.8rem', color: 'var(--text-main)', margin: 0 }}>{t('Bildirim Merkezi')}</h1>
          <p style={{ color: 'var(--text-muted)', margin: '0.2rem 0 0 0', fontSize: '0.95rem' }}>
            {t('Güncel durumları ve bilgilendirmeleri takip edin.')}
          </p>
        </div>
      </div>

      <div className={styles.card} style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <div className={styles.spinner} style={{ margin: '0 auto 1rem' }}></div>
            {t('Bildirimler yükleniyor...')}
          </div>
        ) : notifications.length === 0 ? (
          <div style={{ padding: '4rem 2rem', textAlign: 'center', color: 'var(--text-muted)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
             <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            <span>{t('Şu an için yeni bir bildiriminiz bulunmuyor.')}</span>
          </div>
        ) : (
          notifications.map(notif => (
            <div 
              key={notif.id} 
              onClick={() => {
                if (!notif.read) handleMarkAsRead(notif.id);
              }}
              style={{ 
                padding: '1.25rem', 
                borderRadius: '12px',
                backgroundColor: notif.read ? 'var(--surface-hover)' : 'rgba(59, 130, 246, 0.05)',
                borderLeft: notif.read ? '4px solid transparent' : '4px solid var(--primary)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                transition: 'all 0.2s ease',
                boxShadow: notif.read ? 'none' : '0 2px 8px rgba(0,0,0,0.05)',
                cursor: notif.read ? 'default' : 'pointer'
              }}
            >
              <div style={{ flex: 1, paddingRight: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.4rem' }}>
                  {!notif.read && <span style={{ padding: '2px 8px', fontSize: '0.7rem', fontWeight: 'bold', backgroundColor: 'var(--primary)', color: 'white', borderRadius: '12px', letterSpacing: '0.5px' }}>{t('YENİ')}</span>}
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    {new Date(notif.createdAt).toLocaleString('tr-TR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
                <p style={{ color: notif.read ? 'var(--text-muted)' : 'var(--text-main)', margin: 0, fontSize: '0.95rem', lineHeight: '1.5' }}>
                  {notif.message}
                </p>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
