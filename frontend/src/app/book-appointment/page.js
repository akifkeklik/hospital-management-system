'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { AppointmentService, DepartmentService, DoctorService } from '../../services/api';
import { toast } from '../../components/Toast';
import { useAuth } from '../../context/AuthContext';
import { useSettings } from '../../context/SettingsContext';
import SymptomAnalyzer from '../../components/SymptomAnalyzer';
import HospitalMap from '../../components/HospitalMap';
import styles from './page.module.css';

export default function BookAppointment() {
  const router = useRouter();
  const { t, tErr } = useSettings();
  const [step, setStep] = useState(0); // 0 = AI Asistan, 1-5 = mevcut adımlar
  const [loading, setLoading] = useState(false);
  const { user: userProfile } = useAuth();

  // Data states
  const [departments, setDepartments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);

  // Selection states
  const [selectedDept, setSelectedDept] = useState(null);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [notes, setNotes] = useState('');
  const [showMap, setShowMap] = useState(false);

  useEffect(() => {
    async function init() {
      try {
        const depts = await DepartmentService.getAll(0, 100);
        setDepartments(depts.content || []);
      } catch (error) {
        console.error("Init error:", error);
      }
    }
    init();
  }, []);

  const handleDeptSelect = async (dept) => {
    setSelectedDept(dept);
    setSelectedDoctor(null);
    setSelectedDate('');
    setSelectedTime('');
    setStep(2);
    setLoading(true);
    try {
      const docs = await DoctorService.getAll(0, 100);
      const filteredDocs = (docs.content || []).filter(d => d.departmentId === dept.id);
      setDoctors(filteredDocs);
    } catch (error) {
      console.error("Doctors load error:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDoctorSelect = (doctor) => {
    setSelectedDoctor(doctor);
    setSelectedDate('');
    setSelectedTime('');
    setStep(3);
  };

  const handleDateSelect = async (e) => {
    const date = e.target.value;
    setSelectedDate(date);
    setSelectedTime('');
    
    if (!date) return;

    setLoading(true);
    try {
      const slots = await AppointmentService.getAvailableSlots(selectedDoctor.id, date);
      setAvailableSlots(slots);
    } catch (error) {
      console.error("Slots load error:", error);
      setAvailableSlots([]);
    } finally {
      setLoading(false);
    }
  };

  const handleTimeSelect = (time) => {
    setSelectedTime(time);
    setStep(4);
  };

  const handleSubmit = async () => {
    if (!userProfile || !selectedDoctor || !selectedDate || !selectedTime) return;
    
    setLoading(true);
    try {
      const appointmentDate = `${selectedDate}T${selectedTime}:00`;
      await AppointmentService.create({
        patientId: userProfile.id,
        doctorId: selectedDoctor.id,
        appointmentDate,
        notes
      });
      setStep(5);
    } catch (error) {
      toast.error(t('appointment_error') + ": " + error.message);
    } finally {
      setLoading(false);
    }
  };

  // Progress bar adım sayısı (AI adımı 0 olduğundan 1-4 arası gösterilir)
  const displayStep = Math.max(step, 1);

  return (
    <div className={styles.wizardContainer}>
      {/* Progress Bar — sadece step >= 1 iken göster */}
      {step >= 1 && (
        <div className={styles.progressContainer}>
          {[1, 2, 3, 4].map(s => (
            <div key={s} className={`${styles.progressStep} ${displayStep >= s ? styles.active : ''}`}>
              <div className={styles.stepCircle}>{s}</div>
              <div className={styles.stepLabel}>
                {s === 1 ? t('department') : s === 2 ? t('doctor') : s === 3 ? t('date_and_time') : t('confirmation')}
              </div>
            </div>
          ))}
          <div className={styles.progressLine} style={{ width: `${(Math.min(displayStep, 4) - 1) * 33.33}%` }}></div>
        </div>
      )}

      <div className={styles.stepContent}>
        {/* Adım 0: AI Semptom Analizi */}
        {step === 0 && (
          <div className={styles.animationFadeIn}>
            <SymptomAnalyzer
              departments={departments}
              onDepartmentSelect={(dept) => handleDeptSelect(dept)}
              onSkip={() => setStep(1)}
            />
          </div>
        )}

        {/* Adım 1: Bölüm Seçimi */}
        {step === 1 && (
          <div className={styles.animationFadeIn}>
            <h2 className={styles.stepTitle}>{t('select_dept_question')}</h2>
            <div className={styles.gridContainer}>
              {departments.map(dept => (
                <div key={dept.id} className={styles.selectionCard} onClick={() => handleDeptSelect(dept)}>
                  <div className={styles.cardIcon}>🏢</div>
                  <h3 style={{ textTransform: 'uppercase' }}>{dept.name}</h3>
                  <p>{dept.description || t('disease_diagnosis_treatment')}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Adım 2: Doktor Seçimi */}
        {step === 2 && (
          <div className={styles.animationFadeIn}>
            <button className={styles.backBtn} onClick={() => setStep(1)}>← {t('go_back')}</button>
            <h2 className={styles.stepTitle} style={{ textTransform: 'uppercase' }}>{selectedDept?.name} - {t('dept_doctors_title')}</h2>
            {loading ? <p>{t('loading')}</p> : doctors.length === 0 ? (
              <p>{t('no_doctors_in_dept')}</p>
            ) : (
              <div className={styles.gridContainer}>
                {doctors.map(doc => (
                  <div key={doc.id} className={styles.selectionCard} onClick={() => handleDoctorSelect(doc)}>
                    <div className={styles.avatarCircle}>{doc.firstName.charAt(0)}</div>
                    <h3>Dr. {doc.firstName} {doc.lastName}</h3>
                    <p>{doc.title || t('specialist_doctor')}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Adım 3: Tarih ve Saat Seçimi */}
        {step === 3 && (
          <div className={styles.animationFadeIn}>
            <button className={styles.backBtn} onClick={() => setStep(2)}>← {t('go_back')}</button>
            <h2 className={styles.stepTitle}>{t('select_date_time')}</h2>
            <p className={styles.selectedDoctorInfo}>{t('selected_doctor')}: Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</p>
            
            <div className={styles.dateSelector}>
              <label>{t('select_date')}:</label>
              <input 
                type="date" 
                value={selectedDate} 
                onChange={handleDateSelect}
                min={new Date().toISOString().split('T')[0]}
                className={styles.dateInput}
              />
            </div>

            {selectedDate && (
              <div className={styles.slotsContainer}>
                <h3>{t('available_slots')} ({selectedDate})</h3>
                {loading ? <p>{t('checking_slots')}</p> : availableSlots.length === 0 ? (
                  <p className={styles.noSlotsMsg}>{t('no_slots_available')}</p>
                ) : (
                  <div className={styles.slotsGrid}>
                    {availableSlots.map(time => (
                      <button 
                        key={time} 
                        className={styles.slotBtn}
                        onClick={() => handleTimeSelect(time)}
                      >
                        {time}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* Adım 4: Onay */}
        {step === 4 && (
          <div className={styles.animationFadeIn}>
            <button className={styles.backBtn} onClick={() => setStep(3)}>← {t('go_back')}</button>
            <h2 className={styles.stepTitle}>{t('appointment_summary')}</h2>
            
            <div className={styles.summaryCard}>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>{t('department')}:</span>
                <span className={styles.summaryValue}>{selectedDept?.name}</span>
              </div>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>{t('doctor')}:</span>
                <span className={styles.summaryValue}>Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</span>
              </div>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>{t('date_and_time')}:</span>
                <span className={styles.summaryValue}>{selectedDate} / {selectedTime}</span>
              </div>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>{t('patient')}:</span>
                <span className={styles.summaryValue}>{userProfile?.firstName} {userProfile?.lastName}</span>
              </div>
            </div>

            <div className={styles.notesContainer}>
              <label>{t('doctor_note_label')}:</label>
              <textarea 
                value={notes} 
                onChange={e => setNotes(e.target.value)}
                placeholder={t('complaint_placeholder')}
                rows="3"
                className={styles.notesInput}
              />
            </div>

            <button 
              className={styles.confirmBtn} 
              onClick={handleSubmit} 
              disabled={loading}
            >
              {loading ? t('processing') : t('confirm_appointment')}
            </button>
          </div>
        )}

        {/* Adım 5: Başarılı */}
        {step === 5 && (
          <div className={styles.successContainer}>
            <div className={styles.successIcon}>✅</div>
            <h2 className={styles.stepTitle}>{t('appointment_success')}</h2>
            <p>{t('appointment_success_detail')}</p>
            <div className={styles.successDetails}>
              <p><strong>{t('department')}:</strong> {selectedDept?.name}</p>
              <p><strong>{t('doctor')}:</strong> Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</p>
              <p><strong>{t('date')}:</strong> {selectedDate} {t('select_time')}: {selectedTime}</p>
            </div>
            
            <button onClick={() => setShowMap(true)} className={styles.mapViewBtn} style={{ marginBottom: '1rem', background: 'none', border: '1px solid var(--border)', color: 'var(--primary)', padding: '0.5rem 1rem', borderRadius: '8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '1rem auto' }}>
              📍 {t('view_on_map')}
            </button>

            <button className={styles.confirmBtn} onClick={() => router.push('/')}>
              {t('go_home')}
            </button>

            <HospitalMap departmentName={selectedDept?.name} isOpen={showMap} onClose={() => setShowMap(false)} />
          </div>
        )}
      </div>
    </div>
  );
}
