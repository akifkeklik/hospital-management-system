'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { AppointmentService, AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import ConfirmModal from './ConfirmModal';
import EmptyState from './EmptyState';
import { toast } from './Toast';

export default function PatientDashboard() {
  const { t } = useSettings();
  const router = useRouter();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [userProfile, setUserProfile] = useState(null);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [quickSearch, setQuickSearch] = useState('');
  const [timeFilter, setTimeFilter] = useState('all');

  const fetchData = async () => {
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
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCancelClick = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeCancel = async () => {
    try {
      await AppointmentService.updateStatus(confirmModal.id, 'CANCELLED');
      toast.success('Randevunuz başarıyla iptal edildi.');
      fetchData(); // Listeyi yenile
    } catch (error) {
      toast.error('İptal işlemi başarısız: ' + error.message);
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

  return (
    <div style={containerStyle}>
      <div style={heroBannerStyle}>
        <div style={heroContentStyle}>
          <h1 style={heroTitleStyle}>
            {t('hero_welcome')} <br/>
            <span style={{ color: 'var(--primary)' }}>{userProfile?.firstName ? userProfile.firstName : t('patient')}</span>
          </h1>
          <p style={heroSubtitleStyle}>
            {t('patient_dashboard_subtitle')}
          </p>
          
          <form onSubmit={handleQuickSearch} style={heroSearchFormStyle}>
            <div style={heroSearchIconStyle}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <input 
              type="text"
              placeholder={t('hero_search_placeholder')}
              value={quickSearch}
              onChange={(e) => setQuickSearch(e.target.value)}
              style={heroSearchInputStyle}
            />
            <button type="submit" style={heroSearchBtnStyle}>Ara</button>
          </form>
        </div>
        
        <div style={heroActionStyle}>
          <Link href="/book-appointment" style={bigBookBtnStyle}>
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

      <div style={upcomingSectionStyle}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--text-main)', margin: 0 }}>{t('upcoming_appointments')}</h2>
          <select 
            value={timeFilter} 
            onChange={(e) => setTimeFilter(e.target.value)}
            style={{ padding: '0.4rem', borderRadius: '8px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.85rem' }}
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
          <p style={emptyStateStyle}>{t('loading')}</p>
        ) : appointments.filter(app => {
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
        }).length === 0 ? (
          <EmptyState 
            title={t('empty_state_title')} 
            description={t('empty_state_desc')} 
          />
        ) : (
          <div style={appointmentsGridStyle}>
            {appointments.filter(app => {
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
            }).map(app => (
              <div key={app.id} style={appointmentCardStyle}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={appDateStyle}>{formatDate(app.appointmentDate)}</div>
                    <div style={appDoctorStyle}>Dr. {app.doctorFullName}</div>
                    <div style={appDeptStyle}>{t(app.departmentName)}</div>
                    {app.notes && <div style={appNotesStyle}>{t('appointment_notes')}: {app.notes}</div>}
                  </div>
                  <button 
                    onClick={() => handleCancelClick(app.id)} 
                    style={cancelBtnStyle}
                    title="Randevuyu İptal Et"
                  >
                    İptal Et
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title="Randevu İptali"
        message="Bu randevuyu iptal etmek istediğinize emin misiniz? Bu işlem geri alınamaz."
        onConfirm={executeCancel}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText="Evet, İptal Et"
        type="danger"
      />
    </div>
  );
}

// Styles
const containerStyle = {
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
  flex: 1,
  minHeight: 0
};

const heroBannerStyle = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  flexWrap: 'wrap',
  gap: '2rem',
  padding: '3rem',
  borderRadius: '24px',
  background: 'linear-gradient(135deg, rgba(var(--surface-rgb), 0.8), rgba(var(--background-rgb), 1))',
  border: '1px solid rgba(var(--primary-rgb), 0.2)',
  boxShadow: '0 20px 40px -15px rgba(var(--primary-rgb), 0.1)',
  marginBottom: '2rem',
  position: 'relative',
  overflow: 'hidden'
};

const heroContentStyle = {
  flex: '1 1 400px',
  zIndex: 2
};

const heroActionStyle = {
  flexShrink: 0,
  zIndex: 2
};

const heroTitleStyle = {
  fontSize: '2.5rem',
  fontWeight: '800',
  color: 'var(--text-main)',
  lineHeight: '1.2',
  margin: '0 0 1rem 0'
};

const heroSubtitleStyle = {
  fontSize: '1.1rem',
  color: 'var(--text-muted)',
  marginBottom: '2rem'
};

const heroSearchFormStyle = {
  display: 'flex',
  position: 'relative',
  maxWidth: '500px',
  boxShadow: '0 8px 20px -5px rgba(0,0,0,0.1)',
  borderRadius: '12px',
  overflow: 'hidden',
  border: '1px solid var(--border)'
};

const heroSearchIconStyle = {
  position: 'absolute',
  left: '16px',
  top: '50%',
  transform: 'translateY(-50%)',
  color: 'var(--text-muted)',
  display: 'flex'
};

const heroSearchInputStyle = {
  width: '100%',
  padding: '1rem 1rem 1rem 3rem',
  border: 'none',
  outline: 'none',
  fontSize: '1rem',
  backgroundColor: 'var(--surface)',
  color: 'var(--text-main)'
};

const heroSearchBtnStyle = {
  padding: '0 1.5rem',
  backgroundColor: 'var(--primary)',
  color: 'white',
  border: 'none',
  fontWeight: 'bold',
  cursor: 'pointer',
  transition: 'background-color 0.2s'
};

const bigBookBtnStyle = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  backgroundColor: 'var(--primary)',
  color: '#ffffff',
  padding: '0.75rem 1.5rem',
  fontSize: '1rem',
  fontWeight: '700',
  borderRadius: '12px',
  textDecoration: 'none',
  boxShadow: '0 4px 6px -1px rgba(var(--primary-rgb), 0.3)',
  transition: 'transform 0.2s, box-shadow 0.2s',
  flexShrink: 0
};

const upcomingSectionStyle = {
  display: 'flex',
  flexDirection: 'column',
  flex: 1,
  minHeight: 0,
  backgroundColor: 'var(--surface)',
  borderRadius: '16px',
  border: '1px solid var(--border)',
  padding: '1.5rem',
  overflow: 'hidden'
};

const sectionTitleStyle = {
  fontSize: '1.25rem',
  fontWeight: '700',
  color: 'var(--text-main)',
  borderBottom: '1px solid var(--border)',
  paddingBottom: '0.5rem',
  marginBottom: '1rem',
  flexShrink: 0
};

const emptyStateStyle = {
  color: 'var(--text-muted)',
  fontStyle: 'italic'
};

const emptyCardStyle = {
  backgroundColor: 'var(--surface)',
  border: '1px dashed var(--border)',
  borderRadius: '12px',
  padding: '4rem 2rem',
  textAlign: 'center',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '1rem',
  color: 'var(--text-muted)'
};

const emptyIconStyle = {
  fontSize: '3rem',
  opacity: '0.5'
};

const appointmentsGridStyle = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
  gap: '1.25rem',
  overflowY: 'auto',
  flex: 1
};

const appointmentCardStyle = {
  backgroundColor: 'var(--surface)',
  border: '1px solid var(--border)',
  borderLeft: '4px solid var(--primary)',
  borderRadius: '8px',
  padding: '1.5rem',
  boxShadow: 'var(--shadow-sm)',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  transition: 'transform 0.2s'
};

const appDateStyle = {
  fontSize: '1.1rem',
  fontWeight: '700',
  color: 'var(--text-main)'
};

const appDoctorStyle = {
  fontSize: '1rem',
  fontWeight: '600',
  marginBottom: '0.25rem',
  color: 'var(--text-main)'
};

const appDeptStyle = {
  fontSize: '0.9rem',
  color: 'var(--text-muted)',
  marginBottom: '0.5rem',
  display: 'inline-block',
  backgroundColor: 'var(--background)',
  padding: '0.2rem 0.5rem',
  borderRadius: '4px'
};

const appNotesStyle = {
  fontSize: '0.85rem',
  fontStyle: 'italic',
  color: 'var(--text-muted)',
  marginTop: '0.5rem',
  borderTop: '1px dashed var(--border)',
  paddingTop: '0.5rem'
};

const cancelBtnStyle = {
  backgroundColor: '#fef2f2',
  color: '#ef4444',
  border: '1px solid #fca5a5',
  padding: '0.4rem 0.8rem',
  borderRadius: '6px',
  fontSize: '0.85rem',
  fontWeight: '600',
  cursor: 'pointer',
  transition: 'all 0.2s'
};
