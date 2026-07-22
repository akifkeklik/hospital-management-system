'use client';
import { useState, useEffect } from 'react';
import { useSettings } from '../../../context/SettingsContext';
import { fetchAPI } from '../../../services/api';
import { toast } from '../../../components/Toast';
import ConfirmModal from '../../../components/ConfirmModal';
import styles from './page.module.css';

export default function DoctorRequestsPage() {
  const { t, tErr } = useSettings();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmModal, setConfirmModal] = useState({ show: false, type: '', reqId: null, message: '' });
  const [credentialsModal, setCredentialsModal] = useState({ show: false, message: '' });

  const fetchRequests = async () => {
    try {
      const data = await fetchAPI('/admin/doctor-requests');
      setRequests(data);
    } catch (err) {
      setError(tErr(err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleAction = async (id, action) => {
    try {
      let successMsg = action === 'approve' ? t('approve_success') : t('reject_success');
      
      const data = await fetchAPI(`/admin/doctor-requests/${id}/${action}`, {
        method: 'POST'
      });
      
      if (data && data.message) {
        successMsg = data.message;
      }
      
      setConfirmModal({ show: false, type: '', reqId: null, message: '' });
      
      if (action === 'approve') {
        setCredentialsModal({ show: true, message: successMsg });
      } else {
        toast.success(successMsg);
      }
      
      fetchRequests(); // Refresh list
    } catch (err) {
      setConfirmModal({ show: false, type: '', reqId: null, message: '' });
      toast.error(tErr(err.message));
    }
  };

  const executeConfirm = () => {
    if (confirmModal.reqId && confirmModal.type) {
      handleAction(confirmModal.reqId, confirmModal.type);
    }
  };

  if (loading) return <div>{t('loading')}</div>;
  if (error) return <div style={{ color: 'red' }}>{error}</div>;

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>{t('doctor_requests_title')}</h1>
      
      {requests.length === 0 ? (
        <div className={styles.emptyState}>
          <div className={styles.emptyStateIconWrapper}>
            <i className="fi fi-rr-inbox"></i>
          </div>
          <h2 className={styles.emptyStateTitle}>{t('no_pending_requests')}</h2>
          <p className={styles.emptyStateDesc}>{t('no_pending_requests_desc')}</p>
        </div>
      ) : (
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>{t('date')}</th>
                <th>{t('title_desc')}</th>
                <th>{t('tc_no')}</th>
                <th>{t('specialization')}</th>
                <th>{t('email')}</th>
                <th>{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {requests.map(req => (
                <tr key={req.id}>
                  <td>{new Date(req.requestDate).toLocaleDateString()}</td>
                  <td>{req.firstName} {req.lastName}</td>
                  <td>{req.tcIdentityNumber}</td>
                  <td>{req.specialization || '-'}</td>
                  <td>{req.email}</td>
                  <td>
                    <div className={styles.actionButtons}>
                      <button 
                        className={styles.approveBtn}
                        onClick={() => setConfirmModal({ show: true, type: 'approve', reqId: req.id, message: t('approve_confirm') })}
                        title={t('approve')}
                      >
                        <i className="fi fi-rr-check"></i>
                      </button>
                      <button 
                        className={styles.rejectBtn}
                        onClick={() => setConfirmModal({ show: true, type: 'reject', reqId: req.id, message: t('reject_confirm') })}
                        title={t('reject')}
                      >
                        <i className="fi fi-rr-cross"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Confirmation Modal */}
      {confirmModal.show && (
        <div className={styles.modalOverlay}>
          <div className={styles.modalContent}>
            <h3>{t('confirm_action')}</h3>
            <p>{confirmModal.message}</p>
            <div className={styles.modalActions}>
              <button 
                className={styles.cancelBtn}
                onClick={() => setConfirmModal({ show: false, type: '', reqId: null, message: '' })}
              >
                {t('cancel')}
              </button>
              <button 
                className={confirmModal.type === 'approve' ? styles.confirmApproveBtn : styles.confirmRejectBtn}
                onClick={executeConfirm}
              >
                {t('confirm')}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Credentials Modal */}
      {credentialsModal.show && (
        <div className={styles.modalOverlay}>
          <div className={styles.modalContent} style={{ maxWidth: '500px', borderTop: '4px solid #10b981' }}>
            <h3 style={{ color: '#10b981', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <i className="fi fi-rr-check-circle"></i> Onay Başarılı
            </h3>
            <p style={{ marginTop: '1rem', fontSize: '1.1rem', lineHeight: '1.5', padding: '1rem', backgroundColor: 'var(--bg-lighter)', borderRadius: '8px', wordBreak: 'break-all' }}>
              {credentialsModal.message}
            </p>
            <div className={styles.modalActions} style={{ marginTop: '1.5rem', justifyContent: 'center' }}>
              <button 
                className={styles.confirmApproveBtn}
                onClick={() => setCredentialsModal({ show: false, message: '' })}
                style={{ width: '100%', padding: '0.75rem' }}
              >
                Anladım, Kapat
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
