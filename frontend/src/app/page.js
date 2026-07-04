'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { DepartmentService, PatientService, DoctorService, AppointmentService, NotificationService } from '../services/api';
import DashboardCharts from '../components/DashboardCharts';
import PatientDashboard from '../components/PatientDashboard';
import DoctorDashboard from '../components/DoctorDashboard';
import { useSettings } from '../context/SettingsContext';
import styles from './page.module.css';
import Modal from '../components/Modal';
import { toast } from '../components/Toast';

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
}

export default function Dashboard() {
  const { t } = useSettings();
  const [stats, setStats] = useState({
    departments: 0,
    patients: 0,
    doctors: 0,
    appointments: 0
  });
  const [chartData, setChartData] = useState({
    departments: [],
    doctors: [],
    appointments: []
  });
  const [loading, setLoading] = useState(true);
  const [role, setRole] = useState(null);
  
  // Announcement State
  const [isAnnouncementModalOpen, setIsAnnouncementModalOpen] = useState(false);
  const [announcementMessage, setAnnouncementMessage] = useState('');
  const [sendingAnnouncement, setSendingAnnouncement] = useState(false);

  const handleSendAnnouncement = async (e) => {
    e.preventDefault();
    if (!announcementMessage.trim()) return;
    
    setSendingAnnouncement(true);
    try {
      await NotificationService.broadcastToAll(announcementMessage);
      toast.success(t('announcement_sent'));
      setIsAnnouncementModalOpen(false);
      setAnnouncementMessage('');
    } catch (error) {
      toast.error(t('announcement_error'));
      console.error(error);
    } finally {
      setSendingAnnouncement(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      const decoded = parseJwt(token);
      if (decoded && decoded.role) {
        setRole(decoded.role);
        if (decoded.role === 'ROLE_PATIENT' || decoded.role === 'ROLE_DOCTOR' || decoded.role === 'HEKIM' || decoded.role === 'ROLE_HEKIM' || decoded.role === 'HASTA' || decoded.role === 'ROLE_HASTA') {
          // Hasta veya Doktor ise genel istatistik çekmeye gerek yok, kendi dashboard'ları var
          setLoading(false);
          return;
        }
      }
    }

    async function fetchStats() {
      try {
        const [depts, pats, docs, appts] = await Promise.all([
          DepartmentService.getAll(0, 1000),
          PatientService.getAll(0, 1000),
          DoctorService.getAll(0, 1000),
          AppointmentService.getAll(0, 1000)
        ]);
        
        setStats({
          departments: depts.totalElements || 0,
          patients: pats.totalElements || 0,
          doctors: docs.totalElements || 0,
          appointments: appts.totalElements || 0
        });

        setChartData({
          departments: depts.content || [],
          doctors: docs.content || [],
          appointments: appts.content || []
        });
      } catch (error) {
        console.error("Dashboard istatistikleri alınamadı", error);
      } finally {
        setLoading(false);
      }
    }
    
    fetchStats();
  }, []);

  if (role === 'ROLE_PATIENT' || role === 'PATIENT' || role === 'HASTA' || role === 'ROLE_HASTA') {
    return <PatientDashboard />;
  }

  if (role === 'ROLE_DOCTOR' || role === 'DOCTOR' || role === 'HEKIM' || role === 'ROLE_HEKIM') {
    return <DoctorDashboard />;
  }

  return (
    <div className={styles.dashboard}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className={styles.title} style={{ marginBottom: '0.2rem' }}>{t('welcome')}</h1>
          <p className={styles.subtitle} style={{ margin: 0 }}>{t('welcome_sub')}</p>
        </div>
        <button 
          onClick={() => setIsAnnouncementModalOpen(true)}
          style={{ padding: '0.6rem 1.2rem', backgroundColor: 'var(--primary)', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.5rem', boxShadow: 'var(--shadow-sm)', transition: 'all 0.2s' }}
          onMouseOver={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
          onMouseOut={(e) => e.currentTarget.style.transform = 'translateY(0)'}
        >
          <span style={{ fontSize: '1.2rem' }}>📢</span> Duyuru Yap
        </button>
      </div>
      
      {loading ? (
        <p>{t('loading')}</p>
      ) : (
        <div className={styles.grid}>
          <Link href="/departments" className={styles.card}>
            <div className={styles.cardIcon}>🏢</div>
            <div className={styles.cardInfo}>
              <h3>{t('departments')}</h3>
              <p className={styles.count}>{stats.departments}</p>
            </div>
          </Link>
          
          <Link href="/patients" className={styles.card}>
            <div className={styles.cardIcon}>🧑</div>
            <div className={styles.cardInfo}>
              <h3>{t('registered_patients')}</h3>
              <p className={styles.count}>{stats.patients}</p>
            </div>
          </Link>
          
          <Link href="/doctors" className={styles.card}>
            <div className={styles.cardIcon}>👨‍⚕️</div>
            <div className={styles.cardInfo}>
              <h3>{t('doctors')}</h3>
              <p className={styles.count}>{stats.doctors}</p>
            </div>
          </Link>
          
          <Link href="/appointments" className={styles.card}>
            <div className={styles.cardIcon}>📅</div>
            <div className={styles.cardInfo}>
              <h3>{t('total_appointments')}</h3>
              <p className={styles.count}>{stats.appointments}</p>
            </div>
          </Link>
        </div>
      )}

      {!loading && (
        <DashboardCharts 
          departments={chartData.departments}
          doctors={chartData.doctors}
          appointments={chartData.appointments}
        />
      )}

      {/* Duyuru Modal */}
      <Modal 
        isOpen={isAnnouncementModalOpen} 
        onClose={() => setIsAnnouncementModalOpen(false)} 
        title="Genel Duyuru Yap"
      >
        <form onSubmit={handleSendAnnouncement}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
              Bu mesaj sistemdeki <strong>tüm hekimlere ve hastalara</strong> anlık bildirim olarak gönderilecektir.
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: 'bold', color: 'var(--text-main)' }}>Duyuru Mesajı</label>
              <textarea 
                required 
                value={announcementMessage}
                onChange={(e) => setAnnouncementMessage(e.target.value)}
                placeholder="Örn: Değerli hekimlerimiz ve hastalarımız, sistemimizde bakım çalışması yapılacaktır..."
                style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', minHeight: '120px', resize: 'vertical', fontFamily: 'inherit' }}
              />
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
              <button 
                type="button" 
                onClick={() => setIsAnnouncementModalOpen(false)}
                style={{ padding: '0.6rem 1.2rem', backgroundColor: 'transparent', border: '1px solid var(--border)', borderRadius: '6px', cursor: 'pointer', color: 'var(--text-main)', fontWeight: '600' }}
              >
                İptal
              </button>
              <button 
                type="submit" 
                disabled={sendingAnnouncement}
                style={{ padding: '0.6rem 1.2rem', backgroundColor: 'var(--primary)', color: 'white', border: 'none', borderRadius: '6px', cursor: sendingAnnouncement ? 'not-allowed' : 'pointer', fontWeight: '600', opacity: sendingAnnouncement ? 0.7 : 1 }}
              >
                {sendingAnnouncement ? 'Gönderiliyor...' : 'Paylaş'}
              </button>
            </div>
          </div>
        </form>
      </Modal>
    </div>
  );
}
