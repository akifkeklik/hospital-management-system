'use client';
import { useEffect, useState, useCallback } from 'react';
import { AppointmentService, AuthService, ExaminationService } from '../../../services/api';
import Modal from '../../../components/Modal';
import Pagination from '../../../components/Pagination';
import { toast } from '../../../components/Toast';
import { useApi } from '../../../hooks/useApi';
import { useAuth } from '../../../context/AuthContext';
import styles from '../../shared.module.css';

export default function DoctorQueuePage() {
  const [appointments, setAppointments] = useState([]);
  const [mounted, setMounted] = useState(false);
  const { user: doctorInfo } = useAuth();
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [isExamModalOpen, setIsExamModalOpen] = useState(false);
  const [selectedAppt, setSelectedAppt] = useState(null);
  
  const [diagnosisList, setDiagnosisList] = useState([]);
  const [prescriptionList, setPrescriptionList] = useState([]);

  const [diagForm, setDiagForm] = useState({ icd10Code: '', description: '' });
  const [prescForm, setPrescForm] = useState({ medicationName: '', dosage: '', usageInstruction: '' });

  useEffect(() => {
    setMounted(true);
  }, []);

  const fetchQueue = useCallback(async (signal, currentPage) => {
    const me = doctorInfo;
    if (me && (me.role === 'ROLE_DOCTOR' || me.role === 'DOCTOR' || me.role === 'HEKIM' || me.role === 'ROLE_HEKIM')) {
      const start = new Date();
      start.setHours(0, 0, 0, 0);
      const end = new Date();
      end.setHours(23, 59, 59, 999);
      
      const filters = {
        status: 'SCHEDULED,ARRIVED,IN_EXAMINATION',
        startDate: start.toISOString().split('.')[0],
        endDate: end.toISOString().split('.')[0]
      };

      const myApptsResponse = await AppointmentService.getByDoctor(me.referenceId, currentPage, 100, filters, { signal });
      const myAppts = myApptsResponse.items || [];
      setTotalPages(myApptsResponse.totalPages || 0);
      
      const activeAppts = myAppts.sort((a,b) => new Date(a.appointmentDate) - new Date(b.appointmentDate));
      
      setAppointments(activeAppts);
    }
  }, [doctorInfo]);

  const { loading, execute } = useApi(fetchQueue);

  useEffect(() => {
    execute(page).catch(err => console.error("Error fetching data:", err));
  }, [page, execute]);

  const handleStatusChange = async (id, newStatus) => {
    try {
      await AppointmentService.updateStatus(id, newStatus);
      execute(page).catch(err => console.error(err));
    } catch (error) {
      toast.error(tErr(error.message));
    }
  };

  const openExamModal = async (app) => {
    setSelectedAppt(app);
    setIsExamModalOpen(true);
    // Fetch existing records
    try {
      const diags = await ExaminationService.getDiagnoses(app.id);
      const prescs = await ExaminationService.getPrescriptions(app.id);
      setDiagnosisList(diags || []);
      setPrescriptionList(prescs || []);
    } catch (e) {
      console.error(e);
    }
  };

  const finishExam = async () => {
    await handleStatusChange(selectedAppt.id, 'COMPLETED');
    setIsExamModalOpen(false);
    setSelectedAppt(null);
  };

  const addDiagnosis = async (e) => {
    e.preventDefault();
    try {
      const res = await ExaminationService.addDiagnosis({ ...diagForm, appointmentId: selectedAppt.id });
      setDiagnosisList([...diagnosisList, res]);
      setDiagForm({ icd10Code: '', description: '' });
    } catch (error) {
      toast.error("Teşhis eklenemedi.");
    }
  };

  const addPrescription = async (e) => {
    e.preventDefault();
    try {
      const res = await ExaminationService.addPrescription({ ...prescForm, appointmentId: selectedAppt.id });
      setPrescriptionList([...prescriptionList, res]);
      setPrescForm({ medicationName: '', dosage: '', usageInstruction: '' });
    } catch (error) {
      toast.error("Reçete eklenemedi.");
    }
  };

  const removeDiagnosis = async (id) => {
    await ExaminationService.deleteDiagnosis(id);
    setDiagnosisList(diagnosisList.filter(d => d.id !== id));
  };

  const removePrescription = async (id) => {
    await ExaminationService.deletePrescription(id);
    setPrescriptionList(prescriptionList.filter(p => p.id !== id));
  };

  if (!mounted) return null;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>Hasta Bekleme Ekranı (Kayıt Kabul - Queue)</h1>
        <p style={{ color: 'var(--text-muted)' }}>Doktor: {doctorInfo?.fullName || 'Yükleniyor...'}</p>
      </div>

      <div style={{ marginTop: '2rem' }}>
        {loading ? (
          <p>Yükleniyor...</p>
        ) : appointments.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', backgroundColor: 'var(--surface)', borderRadius: '12px', border: '1px solid var(--border)' }}>
            <h2 style={{ color: 'var(--text-main)', marginBottom: '1rem' }}>Şu an sırada hasta bulunmuyor.</h2>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {appointments.map(app => (
              <div key={app.id} style={{ 
                display: 'flex', alignItems: 'center', justifyContent: 'space-between', 
                padding: '1.5rem', backgroundColor: app.status === 'IN_EXAMINATION' ? 'rgba(236, 72, 153, 0.05)' : 'var(--surface)', 
                borderRadius: '12px', border: app.status === 'IN_EXAMINATION' ? '2px solid var(--primary)' : '1px solid var(--border)' 
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
                  <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>
                    {new Date(app.appointmentDate).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                  </div>
                  <div>
                    <div style={{ fontSize: '1.2rem', fontWeight: '600', color: 'var(--text-main)' }}>{app.patientFullName}</div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                      Durum: 
                      {app.status === 'ARRIVED' && <span style={{ color: '#8b5cf6', fontWeight: 'bold', marginLeft: '5px' }}>Hastanede Bekliyor</span>}
                      {app.status === 'SCHEDULED' && <span style={{ color: '#3b82f6', fontWeight: 'bold', marginLeft: '5px' }}>Henüz Gelmedi</span>}
                      {app.status === 'IN_EXAMINATION' && <span style={{ color: '#ec4899', fontWeight: 'bold', marginLeft: '5px' }}>Şu An İçeride</span>}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '1rem' }}>
                  {app.status === 'ARRIVED' && (
                    <button 
                      onClick={() => handleStatusChange(app.id, 'IN_EXAMINATION')}
                      style={{ padding: '0.8rem 1.5rem', backgroundColor: 'var(--primary)', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                      Muayeneye Al
                    </button>
                  )}
                  {app.status === 'IN_EXAMINATION' && (
                    <button 
                      onClick={() => openExamModal(app)}
                      style={{ padding: '0.8rem 1.5rem', backgroundColor: '#10b981', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                      Muayene İşlemleri / Bitir
                    </button>
                  )}
                  {app.status === 'SCHEDULED' && (
                    <button 
                      onClick={() => handleStatusChange(app.id, 'NO_SHOW')}
                      style={{ padding: '0.8rem 1.5rem', backgroundColor: '#f59e0b', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                      Gelmedi İşaretle
                    </button>
                  )}
                </div>
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

      {/* MUAYENE MODALI */}
      <Modal isOpen={isExamModalOpen} onClose={() => setIsExamModalOpen(false)} title={`Muayene Detayları - ${selectedAppt?.patientFullName}`}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
          {/* TEŞHİS */}
          <div style={{ backgroundColor: 'var(--surface)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border)' }}>
            <h3 style={{ borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem', marginBottom: '1rem' }}>Tanı / Teşhis (ICD-10)</h3>
            
            <form onSubmit={addDiagnosis} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
              <input required placeholder="ICD Kodu (Örn: J01)" value={diagForm.icd10Code} onChange={e => setDiagForm({...diagForm, icd10Code: e.target.value})} style={{ width: '30%' }} />
              <input required placeholder="Açıklama" value={diagForm.description} onChange={e => setDiagForm({...diagForm, description: e.target.value})} style={{ flex: 1 }} />
              <button type="submit" style={{ backgroundColor: 'var(--primary)', color: 'white', border: 'none', padding: '0 1rem', borderRadius: '4px' }}>Ekle</button>
            </form>

            <ul style={{ listStyle: 'none', padding: 0 }}>
              {diagnosisList.map(d => (
                <li key={d.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem', borderBottom: '1px solid var(--border)' }}>
                  <span><strong>{d.icd10Code}</strong> - {d.description}</span>
                  <button onClick={() => removeDiagnosis(d.id)} style={{ color: 'var(--danger)', background: 'none', border: 'none', cursor: 'pointer' }}>Sil</button>
                </li>
              ))}
            </ul>
          </div>

          {/* REÇETE */}
          <div style={{ backgroundColor: 'var(--surface)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border)' }}>
            <h3 style={{ borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem', marginBottom: '1rem' }}>E-Reçete İlaçları</h3>
            
            <form onSubmit={addPrescription} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1rem' }}>
              <input required placeholder="İlaç Adı" value={prescForm.medicationName} onChange={e => setPrescForm({...prescForm, medicationName: e.target.value})} />
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <input required placeholder="Dozaj (Örn: 1000mg)" value={prescForm.dosage} onChange={e => setPrescForm({...prescForm, dosage: e.target.value})} style={{ width: '40%' }} />
                <input required placeholder="Kullanım (Örn: Günde 2 Tok)" value={prescForm.usageInstruction} onChange={e => setPrescForm({...prescForm, usageInstruction: e.target.value})} style={{ flex: 1 }} />
              </div>
              <button type="submit" style={{ backgroundColor: 'var(--primary)', color: 'white', border: 'none', padding: '0.5rem', borderRadius: '4px' }}>İlaç Ekle</button>
            </form>

            <ul style={{ listStyle: 'none', padding: 0 }}>
              {prescriptionList.map(p => (
                <li key={p.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem', borderBottom: '1px solid var(--border)' }}>
                  <div>
                    <strong>{p.medicationName}</strong> ({p.dosage})
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{p.usageInstruction}</div>
                  </div>
                  <button onClick={() => removePrescription(p.id)} style={{ color: 'var(--danger)', background: 'none', border: 'none', cursor: 'pointer' }}>Sil</button>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div style={{ marginTop: '2rem', textAlign: 'right', borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
          <button 
            onClick={finishExam}
            style={{ padding: '0.8rem 2rem', backgroundColor: '#10b981', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', fontSize: '1.1rem', cursor: 'pointer' }}
          >
            Muayeneyi Tamamla ve Kapat
          </button>
        </div>
      </Modal>
    </div>
  );
}
