'use client';
import { useEffect, useState, useCallback } from 'react';
import { AppointmentService, AuthService, ExaminationService } from '../../services/api';
import Modal from '../../components/Modal';
import Pagination from '../../components/Pagination';
import { useSettings } from '../../context/SettingsContext';
import { useAuth } from '../../context/AuthContext';
import { useApi } from '../../hooks/useApi';
import styles from '../shared.module.css';

export default function PatientRecordsPage() {
  const { t, language } = useSettings();
  const [appointments, setAppointments] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const { user: me } = useAuth();

  const [isExamModalOpen, setIsExamModalOpen] = useState(false);
  const [selectedAppt, setSelectedAppt] = useState(null);
  const [diagnosisList, setDiagnosisList] = useState([]);
  const [prescriptionList, setPrescriptionList] = useState([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  const fetchRecords = useCallback(async (signal, currentPage) => {
    if (me && me.role === 'ROLE_PATIENT') {
      const myApptsResponse = await AppointmentService.getByPatient(me.referenceId, currentPage, 100, { status: 'COMPLETED' }, { signal });
      const myAppts = myApptsResponse.items || [];
      setTotalPages(myApptsResponse.totalPages || 0);
      // Backend'den COMPLETED olanlar geliyor, sadece sırala
      const completed = myAppts.sort((a,b) => new Date(b.appointmentDate) - new Date(a.appointmentDate));
      setAppointments(completed);
    }
  }, [me]);

  const { loading, execute } = useApi(fetchRecords);

  useEffect(() => {
    execute(page).catch(err => console.error("Error fetching data:", err));
  }, [page, execute]);

  const openDetails = async (app) => {
    setSelectedAppt(app);
    setIsExamModalOpen(true);
    try {
      const diags = await ExaminationService.getDiagnoses(app.id);
      const prescs = await ExaminationService.getPrescriptions(app.id);
      setDiagnosisList(diags || []);
      setPrescriptionList(prescs || []);
    } catch (e) {
      console.error(e);
    }
  };

  if (!mounted) return null;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>{t('medical_records_title')}</h1>
      </div>

      <div style={{ marginTop: '2rem' }}>
        {loading ? (
          <p>{t('loading')}</p>
        ) : appointments.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', backgroundColor: 'var(--surface)', borderRadius: '12px', border: '1px solid var(--border)' }}>
            <h2 style={{ color: 'var(--text-main)', marginBottom: '1rem' }}>{t('no_medical_records')}</h2>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
            {appointments.map(app => (
              <div key={app.id} style={{ 
                backgroundColor: 'var(--surface)', padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--border)',
                display: 'flex', flexDirection: 'column', gap: '0.5rem'
              }}>
                <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{new Date(app.appointmentDate).toLocaleDateString(language === 'tr' ? 'tr-TR' : 'en-US')}</div>
                <div style={{ color: 'var(--text-muted)' }}>{t('doctor')}: {app.doctorFullName}</div>
                <div style={{ color: 'var(--text-muted)' }}>{t('department')}: {app.departmentName}</div>
                <button 
                  onClick={() => openDetails(app)}
                  style={{ marginTop: '1rem', padding: '0.5rem', backgroundColor: 'var(--primary)', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer' }}
                >
                  {t('view_results_prescriptions')}
                </button>
              </div>
            ))}
          </div>
        )}
        
        <Pagination 
          page={page} 
          totalPages={totalPages} 
          onPageChange={setPage} 
        />
      </div>

      <Modal isOpen={isExamModalOpen} onClose={() => setIsExamModalOpen(false)} title={t('examination_details')}>
        {selectedAppt && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ padding: '1rem', backgroundColor: 'rgba(59, 130, 246, 0.05)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
              <p><strong>{t('date')}:</strong> {new Date(selectedAppt.appointmentDate).toLocaleString(language === 'tr' ? 'tr-TR' : 'en-US')}</p>
              <p><strong>{t('doctor')}:</strong> {selectedAppt.doctorFullName}</p>
              <p><strong>{t('department')}:</strong> {selectedAppt.departmentName}</p>
            </div>

            <div>
              <h3 style={{ borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem', marginBottom: '1rem', color: 'var(--primary)' }}>{t('diagnoses')}</h3>
              {diagnosisList.length === 0 ? (
                <p style={{ color: 'var(--text-muted)' }}>{t('no_diagnosis_record')}</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                  {diagnosisList.map(d => (
                    <li key={d.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid var(--border)' }}>
                      <strong>{d.icd10Code}</strong> - {d.description}
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div>
              <h3 style={{ borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem', marginBottom: '1rem', color: '#10b981' }}>{t('e_prescription')}</h3>
              {prescriptionList.length === 0 ? (
                <p style={{ color: 'var(--text-muted)' }}>{t('no_prescription_record')}</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                  {prescriptionList.map(p => (
                    <li key={p.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid var(--border)' }}>
                      <div><strong>{p.medicationName}</strong> - {p.dosage}</div>
                      <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>{p.usageInstruction}</div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            
            <button 
              onClick={() => setIsExamModalOpen(false)}
              style={{ alignSelf: 'flex-end', padding: '0.5rem 1.5rem', backgroundColor: 'var(--text-muted)', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer' }}
            >
              {t('close')}
            </button>
          </div>
        )}
      </Modal>
    </div>
  );
}
