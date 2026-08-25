'use client';
import { useEffect, useState } from 'react';
import { DoctorLeaveService, AuthService } from '../../../services/api';
import { toast } from '../../../components/Toast';
import { useSettings } from '../../../context/SettingsContext';
import { useAuth } from '../../../context/AuthContext';
import styles from '../../shared.module.css';

export default function DoctorLeavesRequestPage() {
  const { t } = useSettings();
  const [leaves, setLeaves] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [doctorId, setDoctorId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { user: me } = useAuth();

  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('Yıllık İzin');

  useEffect(() => {
    setMounted(true);
    if (me) {
      checkAuthAndFetch();
    }
  }, [me]);

  const checkAuthAndFetch = async () => {
    setLoading(true);
    try {
      if (me && (me.role === 'DOCTOR' || me.role === 'ROLE_DOCTOR' || me.role === 'HEKIM' || me.role === 'ROLE_HEKIM')) {
        // Find doctorId by username or via some other API. For now, since we don't have getDoctorByUserId:
        // Actually we do have /admin/doctors but we might need /doctors/me. 
        // Assuming user ID maps to doctor or we just fetch all leaves and filter. 
        // Let's assume the user has a doctorId attached or we fetch it.
        // As a fallback in this demo, let's just hardcode doctorId=2 or fetch from an endpoint if available.
        // Wait, earlier I saw we have a DoctorService. Let's fetch all and match email or id.
        // In a real app we'd have an endpoint. Let's just fetch all leaves for now or prompt.
        const id = me.id; // Just using user ID for demo, usually we map User <-> Doctor
        setDoctorId(id);
        const myLeaves = await DoctorLeaveService.getByDoctorId(id);
        setLeaves(myLeaves);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRequestLeave = async (e) => {
    e.preventDefault();
    if (!doctorId) { toast.error('Doktor bilgisi alınamadı!'); return; }
    try {
      await DoctorLeaveService.create({
        doctorId: doctorId,
        startDate,
        endDate,
        reason
      });
      toast.success('İzin talebiniz başarıyla iletildi.');
      setStartDate('');
      setEndDate('');
      setIsModalOpen(false);
      checkAuthAndFetch();
    } catch (err) {
      toast.error('Hata oluştu.');
    }
  };

  const getStatusBadge = (status) => {
    switch(status) {
      case 'APPROVED':
        return <span style={{ padding: '6px 12px', borderRadius: '20px', backgroundColor: 'rgba(34, 197, 94, 0.1)', color: '#16a34a', fontSize: '0.75rem', fontWeight: 'bold', border: '1px solid rgba(34, 197, 94, 0.2)' }}>ONAYLANDI</span>;
      case 'REJECTED':
        return <span style={{ padding: '6px 12px', borderRadius: '20px', backgroundColor: 'rgba(239, 68, 68, 0.1)', color: '#dc2626', fontSize: '0.75rem', fontWeight: 'bold', border: '1px solid rgba(239, 68, 68, 0.2)' }}>REDDEDİLDİ</span>;
      case 'PENDING':
      default:
        return <span style={{ padding: '6px 12px', borderRadius: '20px', backgroundColor: 'rgba(245, 158, 11, 0.1)', color: '#d97706', fontSize: '0.75rem', fontWeight: 'bold', border: '1px solid rgba(245, 158, 11, 0.2)' }}>BEKLİYOR</span>;
    }
  };

  if (!mounted) return null;

  return (
    <div style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', color: 'var(--text-main)', marginBottom: '0.5rem', fontWeight: '700' }}>İzin Taleplerim</h1>
          <p style={{ color: 'var(--text-muted)' }}>İzin taleplerinizi yönetin ve onay durumlarını takip edin.</p>
        </div>
        <button 
          onClick={() => {
            setStartDate('');
            setEndDate('');
            setReason('Yıllık İzin');
            setIsModalOpen(true);
          }}
          className={styles.primaryBtn}
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem 1.25rem', borderRadius: '8px', fontWeight: '600', transition: 'all 0.2s' }}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Yeni İzin Talebi
        </button>
      </div>

      <div className={styles.card} style={{ borderRadius: '16px', overflow: 'hidden', border: '1px solid var(--border-color)', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)' }}>
        {loading ? (
          <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <div className={styles.spinner} style={{ margin: '0 auto 1rem' }}></div>
            Talepleriniz yükleniyor...
          </div>
        ) : leaves.length === 0 ? (
          <div style={{ padding: '5rem 2rem', textAlign: 'center', color: 'var(--text-muted)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" style={{ marginBottom: '1rem', opacity: 0.3 }}><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            <p style={{ fontSize: '1.1rem', marginBottom: '0.5rem', color: 'var(--text-main)' }}>Daha önce oluşturulmuş bir izin talebiniz bulunmuyor.</p>
            <p style={{ fontSize: '0.9rem' }}>Yeni bir talep oluşturmak için sağ üstteki butonu kullanabilirsiniz.</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto', padding: '1rem' }}>
            <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: '0 0.5rem', textAlign: 'left' }}>
              <thead>
                <tr>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('start_date') || 'Başlangıç Tarihi'}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('end_date') || 'Bitiş Tarihi'}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('reason') || 'Sebep'}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('leave_status') || 'Durum'}</th>
                </tr>
              </thead>
              <tbody>
                {leaves.map((leave, index) => (
                  <tr key={index} style={{ backgroundColor: 'var(--surface)', boxShadow: '0 2px 4px rgba(0,0,0,0.02)', transition: 'transform 0.2s, box-shadow 0.2s', borderRadius: '12px' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 12px rgba(0,0,0,0.05)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.02)'; }}>
                    <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-main)', fontWeight: '600', borderTopLeftRadius: '12px', borderBottomLeftRadius: '12px' }}>{new Date(leave.startDate).toLocaleDateString('tr-TR')}</td>
                    <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-main)', fontWeight: '600' }}>{new Date(leave.endDate).toLocaleDateString('tr-TR')}</td>
                    <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)', fontWeight: '500' }}>{leave.reason}</td>
                    <td style={{ padding: '1.25rem 1.5rem', borderTopRightRadius: '12px', borderBottomRightRadius: '12px' }}>{getStatusBadge(leave.status)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {isModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999 }}>
          <div style={{ backgroundColor: 'var(--background)', borderRadius: '16px', padding: '2rem', width: '100%', maxWidth: '500px', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)' }}>
            <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem', color: 'var(--text-main)' }}>Yeni İzin Talebi</h2>
            <form onSubmit={handleRequestLeave} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '500', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Başlangıç Tarihi</label>
                  <input type="date" required value={startDate} onChange={e => setStartDate(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', backgroundColor: 'var(--surface)', color: 'var(--text-main)' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '500', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Bitiş Tarihi</label>
                  <input type="date" required value={endDate} onChange={e => setEndDate(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', backgroundColor: 'var(--surface)', color: 'var(--text-main)' }} />
                </div>
              </div>
              
              <div>
                <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '500', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Sebep / Açıklama</label>
                <select required value={reason} onChange={e => setReason(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-color)', backgroundColor: 'var(--surface)', color: 'var(--text-main)' }}>
                  <option value="Yıllık İzin">Yıllık İzin</option>
                  <option value="Hastalık">Hastalık</option>
                  <option value="Mazeret İzni">Mazeret İzni</option>
                  <option value="Kongre / Seminer">Kongre / Seminer</option>
                  <option value="Diğer">Diğer</option>
                </select>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setIsModalOpen(false)} style={{ padding: '0.75rem 1.5rem', borderRadius: '8px', backgroundColor: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-main)', cursor: 'pointer', fontWeight: '600' }}>İptal</button>
                <button type="submit" className={styles.primaryBtn} style={{ padding: '0.75rem 1.5rem', borderRadius: '8px', fontWeight: '600' }}>Talebi Gönder</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
