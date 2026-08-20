'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { AppointmentService, AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import ConfirmModal from './ConfirmModal';
import EmptyState from './EmptyState';
import { toast } from './Toast';
import LoadingScreen from './LoadingScreen';
import styles from './PatientDashboard.module.css';

export default function PatientDashboard() {
  const { t, tErr } = useSettings();
  const router = useRouter();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [userProfile, setUserProfile] = useState(null);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [quickSearch, setQuickSearch] = useState('');
  const [timeFilter, setTimeFilter] = useState('all');

  const fetchData = async (retryCount = 0) => {
    try {
      const profile = await AuthService.getMe();
      setUserProfile(profile);

      // Hastanın kendi randevularını çek
      const userAppointments = await AppointmentService.getByPatient(profile.id);
      
      // Sadece gelecek olanları ve "SCHEDULED" olanları filtrele
      const scheduled = userAppointments.filter(app => app.status === 'SCHEDULED');
      
      // Tarihe göre sırala (yaklaşan en üstte)
      scheduled.sort((a, b) => new Date(a.appointmentDate) - new Date(b.appointmentDate));
      
      setAppointments(scheduled);
    } catch (error) {
      console.error("Hasta verileri alınamadı:", error);
      if (retryCount < 2) {
        setTimeout(() => fetchData(retryCount + 1), 3000);
        return; // Retrying, don't set loading false yet
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line
  }, []);

  const handleCancelClick = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeCancel = async () => {
    try {
      await AppointmentService.updateStatus(confirmModal.id, 'CANCELLED');
      toast.success(t('appointment_cancelled'));
      fetchData();
    } catch (error) {
      toast.error(t('cancel_failed') + ': ' + error.message);
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('tr-TR', { 
      year: 'numeric', month: 'long', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  const handleQuickSearch = (e) => {
    e.preventDefault();
    if (quickSearch.trim()) {
      router.push(`/book-appointment?search=${encodeURIComponent(quickSearch.trim())}`);
    }
  };

  const filterByTime = (app) => {
    if (timeFilter === 'all') return true;
    const appDate = new Date(app.appointmentDate);
    const now = new Date();
    const diffMs = appDate - now;
    const diffDays = diffMs / (1000 * 60 * 60 * 24);
    
    if (timeFilter === 'today') return diffDays >= 0 && diffDays < 1;
    if (timeFilter === 'week') return diffDays >= 0 && diffDays <= 7;
    if (timeFilter === 'month') return diffDays >= 0 && diffDays <= 30;
    if (timeFilter === '3months') return diffDays >= 0 && diffDays <= 90;
    if (timeFilter === '6months') return diffDays >= 0 && diffDays <= 180;
    return true;
  };

  const filteredAppointments = appointments.filter(filterByTime);

  return (
    <div className={styles.container}>
      <div className={styles.heroBanner}>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>
            {t('hero_welcome')} <br/>
            <span>{userProfile?.firstName ? userProfile.firstName : t('patient')}</span>
          </h1>
          <p className={styles.heroSubtitle}>
            {t('patient_dashboard_subtitle')}
          </p>
          
          <form onSubmit={handleQuickSearch} className={styles.heroSearchForm}>
            <div className={styles.heroSearchIcon}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <input 
              type="text"
              placeholder={t('hero_search_placeholder')}
              value={quickSearch}
              onChange={(e) => setQuickSearch(e.target.value)}
              className={styles.heroSearchInput}
            />
            <button type="submit" className={styles.heroSearchBtn}>{t('search')}</button>
          </form>
        </div>
        
        <div className={styles.heroAction}>
          <Link href="/book-appointment" className={styles.bigBookBtn}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{marginRight: '8px'}}>
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
              <line x1="12" y1="14" x2="12" y2="18"></line><line x1="8" y1="16" x2="16" y2="16"></line>
            </svg>
            {t('book_appointment')}
          </Link>
        </div>
      </div>

      <div className={styles.upcomingSection}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>{t('upcoming_appointments')}</h2>
          <select 
            value={timeFilter} 
            onChange={(e) => setTimeFilter(e.target.value)}
            className={styles.timeFilter}
          >
            <option value="all">{t('filter_all_time')}</option>
            <option value="today">{t('filter_today')}</option>
            <option value="week">{t('filter_this_week')}</option>
            <option value="month">{t('filter_this_month')}</option>
            <option value="3months">{t('filter_three_months')}</option>
            <option value="6months">{t('filter_six_months')}</option>
          </select>
        </div>
        
        {loading ? (
          <LoadingScreen />
        ) : filteredAppointments.length === 0 ? (
          <EmptyState 
            title={t('empty_state_title')} 
            description={t('empty_state_desc')} 
            customSvg={<span/>}
          />
        ) : (
          <div className={styles.appointmentsGrid}>
            {filteredAppointments.map(app => (
              <div key={app.id} className={styles.appointmentCard}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div className={styles.appDate}>{formatDate(app.appointmentDate)}</div>
                    <div className={styles.appDoctor}>Dr. {app.doctorFullName}</div>
                    <div className={styles.appDept}>{t(app.departmentName)}</div>
                    {app.notes && <div className={styles.appNotes}>{t('appointment_notes')}: {app.notes}</div>}
                  </div>
                  <button 
                    onClick={() => handleCancelClick(app.id)} 
                    className={styles.cancelBtn}
                    title={t('cancel_appointment')}
                  >
                    {t('cancel_appointment_short')}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={t('cancel_appointment_title')}
        message={t('cancel_appointment_confirm_msg')}
        onConfirm={executeCancel}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText={t('yes_cancel')}
        type="danger"
      />
    </div>
  );
}
