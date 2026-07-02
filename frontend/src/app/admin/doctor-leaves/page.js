'use client';
import { useEffect, useState } from 'react';
import { DoctorLeaveService, DoctorService } from '../../../services/api';
import { toast } from '../../../components/Toast';
import styles from '../../shared.module.css';

export default function DoctorLeavesPage() {
  const [leaves, setLeaves] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setMounted(true);
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [leavesData, doctorsData] = await Promise.all([
        DoctorLeaveService.getAll(),
        DoctorService.getAll(0, 100)
      ]);
      setLeaves(leavesData);
      setDoctors(doctorsData.content || []);
    } catch (error) {
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (id, status) => {
    if (confirm(`İzin talebini ${status === 'APPROVED' ? 'Onaylamak' : 'Reddetmek'} istediğinize emin misiniz?`)) {
      try {
        await DoctorLeaveService.updateStatus(id, status);
        fetchData();
      } catch (error) {
        toast.error('İşlem sırasında hata oluştu.');
      }
    }
  };

  if (!mounted) return null;

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', color: 'var(--primary)', marginBottom: '0.5rem' }}>
            İzin Talepleri
          </h1>
          <p style={{ color: '#64748b' }}>
            Doktorların izin taleplerini inceleyin ve onaylayın. Onaylanan izinlerin tarih aralığındaki randevular otomatik iptal edilecektir.
          </p>
        </div>
      </div>

      <div className={styles.card}>
        <div style={{ padding: '1.5rem', borderBottom: '1px solid #e2e8f0' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#1e293b' }}>İzin Talepleri</h2>
        </div>
        <div style={{ overflowX: 'auto' }}>
          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>Yükleniyor...</div>
          ) : leaves.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>Kayıtlı izin talebi bulunmuyor.</div>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Doktor</th>
                  <th>Başlangıç Tarihi</th>
                  <th>Bitiş Tarihi</th>
                  <th>Sebep</th>
                  <th>Durum</th>
                  <th style={{ textAlign: 'right' }}>İşlemler</th>
                </tr>
              </thead>
              <tbody>
                {leaves.map(leave => {
                  const doc = doctors.find(d => d.id === leave.doctorId);
                  return (
                    <tr key={leave.id}>
                      <td>
                        <div style={{ fontWeight: '500', color: '#0f172a' }}>
                          {doc ? `Dr. ${doc.firstName} ${doc.lastName}` : 'Bilinmeyen'}
                        </div>
                      </td>
                      <td>{leave.startDate}</td>
                      <td>{leave.endDate}</td>
                      <td>{leave.reason}</td>
                      <td>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          padding: '0.25rem 0.75rem',
                          borderRadius: '9999px',
                          fontSize: '0.875rem',
                          fontWeight: '500',
                          backgroundColor: leave.status === 'APPROVED' ? '#dcfce7' : leave.status === 'REJECTED' ? '#fee2e2' : '#fef3c7',
                          color: leave.status === 'APPROVED' ? '#166534' : leave.status === 'REJECTED' ? '#991b1b' : '#92400e'
                        }}>
                          {leave.status === 'APPROVED' ? 'Onaylandı' : leave.status === 'REJECTED' ? 'Reddedildi' : 'Bekliyor'}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        {leave.status === 'PENDING' && (
                          <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                            <button
                              onClick={() => handleUpdateStatus(leave.id, 'APPROVED')}
                              style={{
                                padding: '0.375rem 0.75rem',
                                backgroundColor: '#10b981',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontSize: '0.875rem'
                              }}
                            >
                              Onayla
                            </button>
                            <button
                              onClick={() => handleUpdateStatus(leave.id, 'REJECTED')}
                              style={{
                                padding: '0.375rem 0.75rem',
                                backgroundColor: '#ef4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontSize: '0.875rem'
                              }}
                            >
                              Reddet
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
