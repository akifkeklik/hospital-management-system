'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { AppointmentService, DepartmentService, PolyclinicService, DoctorService, PatientService, AuthService } from '../../services/api';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from './page.module.css';

export default function BookAppointment() {
  const router = useRouter();
  const { t } = useSettings();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [userProfile, setUserProfile] = useState(null);

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

  useEffect(() => {
    async function init() {
      try {
        const profile = await AuthService.getMe();
        setUserProfile(profile);
        
        const depts = await DepartmentService.getAll(0, 100);
        setDepartments(depts.content || []);
      } catch (error) {
        console.error("Başlangıç verileri yüklenemedi:", error);
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
      console.error("Doktorlar yüklenemedi", error);
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
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/appointments/available-slots?doctorId=${selectedDoctor.id}&date=${date}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
      });
      if (response.ok) {
        const slots = await response.json();
        setAvailableSlots(slots);
      } else {
        setAvailableSlots([]);
      }
    } catch (error) {
      console.error("Slotlar yüklenemedi", error);
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
      toast.error("Randevu alınırken bir hata oluştu: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.wizardContainer}>
      {/* Progress Bar */}
      <div className={styles.progressContainer}>
        {[1, 2, 3, 4].map(s => (
          <div key={s} className={`${styles.progressStep} ${step >= s ? styles.active : ''}`}>
            <div className={styles.stepCircle}>{s}</div>
            <div className={styles.stepLabel}>
              {s === 1 ? 'Bölüm' : s === 2 ? 'Doktor' : s === 3 ? 'Tarih & Saat' : 'Onay'}
            </div>
          </div>
        ))}
        <div className={styles.progressLine} style={{ width: `${(Math.min(step, 4) - 1) * 33.33}%` }}></div>
      </div>

      <div className={styles.stepContent}>
        {/* Adım 1: Bölüm Seçimi */}
        {step === 1 && (
          <div className={styles.animationFadeIn}>
            <h2 className={styles.stepTitle}>Hangi bölümden randevu almak istiyorsunuz?</h2>
            <div className={styles.gridContainer}>
              {departments.map(dept => (
                <div key={dept.id} className={styles.selectionCard} onClick={() => handleDeptSelect(dept)}>
                  <div className={styles.cardIcon}>🏢</div>
                  <h3>{dept.name}</h3>
                  <p>{dept.description || 'Hastalık teşhis ve tedavi'}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Adım 2: Doktor Seçimi */}
        {step === 2 && (
          <div className={styles.animationFadeIn}>
            <button className={styles.backBtn} onClick={() => setStep(1)}>← Geri Dön</button>
            <h2 className={styles.stepTitle}>{selectedDept?.name} Bölümü Doktorları</h2>
            {loading ? <p>Yükleniyor...</p> : doctors.length === 0 ? (
              <p>Bu bölümde şu an uygun doktor bulunmamaktadır.</p>
            ) : (
              <div className={styles.gridContainer}>
                {doctors.map(doc => (
                  <div key={doc.id} className={styles.selectionCard} onClick={() => handleDoctorSelect(doc)}>
                    <div className={styles.avatarCircle}>{doc.firstName.charAt(0)}</div>
                    <h3>Dr. {doc.firstName} {doc.lastName}</h3>
                    <p>{doc.title || 'Uzman Doktor'}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Adım 3: Tarih ve Saat Seçimi */}
        {step === 3 && (
          <div className={styles.animationFadeIn}>
            <button className={styles.backBtn} onClick={() => setStep(2)}>← Geri Dön</button>
            <h2 className={styles.stepTitle}>Randevu Tarihi ve Saati Belirleyin</h2>
            <p className={styles.selectedDoctorInfo}>Seçilen Doktor: Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</p>
            
            <div className={styles.dateSelector}>
              <label>Tarih Seçin:</label>
              <input 
                type="date" 
                value={selectedDate} 
                onChange={handleDateSelect}
                min={new Date().toISOString().split('T')[0]} // Geçmişi engelle
                className={styles.dateInput}
              />
            </div>

            {selectedDate && (
              <div className={styles.slotsContainer}>
                <h3>Uygun Saatler ({selectedDate})</h3>
                {loading ? <p>Saatler kontrol ediliyor...</p> : availableSlots.length === 0 ? (
                  <p className={styles.noSlotsMsg}>Bu tarihte maalesef uygun randevu saati bulunmuyor. Lütfen başka bir gün seçin.</p>
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
            <button className={styles.backBtn} onClick={() => setStep(3)}>← Geri Dön</button>
            <h2 className={styles.stepTitle}>Randevu Özeti ve Onay</h2>
            
            <div className={styles.summaryCard}>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>Bölüm:</span>
                <span className={styles.summaryValue}>{selectedDept?.name}</span>
              </div>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>Doktor:</span>
                <span className={styles.summaryValue}>Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</span>
              </div>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>Tarih & Saat:</span>
                <span className={styles.summaryValue}>{selectedDate} / {selectedTime}</span>
              </div>
              <div className={styles.summaryRow}>
                <span className={styles.summaryLabel}>Hasta:</span>
                <span className={styles.summaryValue}>{userProfile?.firstName} {userProfile?.lastName}</span>
              </div>
            </div>

            <div className={styles.notesContainer}>
              <label>Doktora iletmek istediğiniz not (isteğe bağlı):</label>
              <textarea 
                value={notes} 
                onChange={e => setNotes(e.target.value)}
                placeholder="Şikayetinizi kısaca belirtebilirsiniz..."
                rows="3"
                className={styles.notesInput}
              />
            </div>

            <button 
              className={styles.confirmBtn} 
              onClick={handleSubmit} 
              disabled={loading}
            >
              {loading ? 'İşleniyor...' : 'Randevuyu Onayla'}
            </button>
          </div>
        )}

        {/* Adım 5: Başarılı */}
        {step === 5 && (
          <div className={styles.successContainer}>
            <div className={styles.successIcon}>✅</div>
            <h2 className={styles.stepTitle}>Randevunuz Başarıyla Alındı!</h2>
            <p>Seçtiğiniz tarih ve saat için yeriniz ayırtılmıştır. Lütfen randevu saatinden 15 dakika önce hastanede bulununuz.</p>
            <div className={styles.successDetails}>
              <p><strong>Bölüm:</strong> {selectedDept?.name}</p>
              <p><strong>Doktor:</strong> Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</p>
              <p><strong>Tarih:</strong> {selectedDate} Saat: {selectedTime}</p>
            </div>
            <button className={styles.confirmBtn} onClick={() => router.push('/')}>
              Ana Sayfaya Dön
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
