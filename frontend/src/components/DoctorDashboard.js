'use client';
import { useState, useEffect } from 'react';
import { AppointmentService, AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import ConfirmModal from './ConfirmModal';
import EmptyState from './EmptyState';
import styles from './DoctorDashboard.module.css'; // Özel stil

export default function DoctorDashboard() {
  const { t } = useSettings();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const userProfile = await AuthService.getMe();
        setProfile(userProfile);

        if (userProfile && userProfile.id) {
            // Randevuları doctorId'ye göre çekeceğiz ama API'de getByDoctor() var mı?
            // AppointmentController.java'da GET /api/appointments/doctor/{doctorId} var mı?
            // Var olduğunu farz ediyoruz. Eğer yoksa, AppointmentService.getAll ve filtreleme yapalım.
            // Fakat backend'i kontrol etmemiz lazım.
            const data = await AppointmentService.getAll(0, 100);
            
            // Eğer backend'de role-based veya doctor spesifik endpoint yoksa, manuel filtre:
            // Sadece bu doktorun randevuları
            const myAppointments = data.content ? data.content.filter(a => a.doctorName === (userProfile.firstName + " " + userProfile.lastName) || a.doctorId === userProfile.id) : [];
            setAppointments(myAppointments);
        }
      } catch (err) {
        console.error(err);
        setError(t('error_loading_data') || 'Veriler yüklenirken hata oluştu.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className={styles.loadingState}>
        <div className={styles.spinner}></div>
        <p>{t('loading') || 'Yükleniyor...'}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.errorState}>
        <div className={styles.errorIcon}>⚠️</div>
        <h3>{error}</h3>
      </div>
    );
  }

  return (
    <div className={styles.dashboardContainer}>
      
      {/* Karşılama Alanı */}
      <div className={styles.welcomeSection}>
        <div>
          <h1 className={styles.welcomeTitle}>{t('welcome') || 'Hoş Geldiniz'}, Dr. {profile?.firstName} {profile?.lastName}</h1>
          <p className={styles.welcomeSubtitle}>{t('doctor_dashboard_subtitle') || 'Bugünkü ve yaklaşan randevularınızı buradan yönetebilirsiniz.'}</p>
        </div>
        <div className={styles.dateBadge}>
          {new Date().toLocaleDateString()}
        </div>
      </div>

      <div className={styles.contentGrid}>
        
        {/* Sol Kolon: Randevular */}
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <h2 className={styles.cardTitle}>
              <span className={styles.icon}>📅</span>
              {t('my_appointments') || 'Randevularım'}
            </h2>
          </div>
          
          <div className={styles.cardBody}>
            {appointments.length === 0 ? (
              <EmptyState 
                icon="☕" 
                title={t('no_appointments_doc') || 'Şu an için hiç randevunuz bulunmuyor.'} 
                description={t('no_appointments_desc') || 'Hastalar randevu aldıkça burada listelenecektir.'} 
              />
            ) : (
              <div className={styles.appointmentList}>
                {appointments.map((app) => (
                  <div key={app.id} className={styles.appointmentItem}>
                    <div className={styles.appointmentTime}>
                      <div className={styles.timeValue}>{app.appointmentTime}</div>
                      <div className={styles.dateValue}>{app.appointmentDate}</div>
                    </div>
                    <div className={styles.appointmentDetails}>
                      <h4>{app.patientName}</h4>
                      <span className={styles.statusBadge}>
                        {t(app.status ? `status_${app.status.toLowerCase()}` : 'status_scheduled') || app.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Sağ Kolon: İstatistikler */}
        <div className={styles.statsCard}>
          <h3>{t('daily_summary') || 'Günlük Özet'}</h3>
          <div className={styles.statItem}>
            <span>{t('total_appointments') || 'Toplam Randevu'}</span>
            <span className={styles.statNumber}>{appointments.length}</span>
          </div>
          
          <div style={{ marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border)' }}>
            <h4 style={{ marginBottom: '1rem', color: 'var(--text-main)', fontSize: '0.95rem' }}>Hızlı İşlemler</h4>
            <a 
              href="/doctor/leaves" 
              style={{ 
                display: 'block', 
                width: '100%', 
                padding: '0.75rem 1rem', 
                backgroundColor: 'var(--primary)', 
                color: 'white', 
                textAlign: 'center', 
                borderRadius: '8px', 
                textDecoration: 'none', 
                fontWeight: '600', 
                transition: 'opacity 0.2s' 
              }}
              onMouseOver={(e) => e.target.style.opacity = 0.9}
              onMouseOut={(e) => e.target.style.opacity = 1}
            >
              📅 İzin Talebi Oluştur
            </a>
          </div>
        </div>
        
      </div>
    </div>
  );
}
