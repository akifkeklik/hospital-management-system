'use client';
import { useState, useEffect } from 'react';
import { AppointmentService, PatientService, DoctorService, DepartmentService, PolyclinicService } from '../../services/api';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import ConfirmModal from '../../components/ConfirmModal';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from '../shared.module.css';

export default function AppointmentsPage() {
  const { t } = useSettings();
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [polyclinics, setPolyclinics] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [formData, setFormData] = useState({ 
    patientId: '', doctorId: '', appointmentDate: '', notes: '' 
  });
  const [selectedDepartmentId, setSelectedDepartmentId] = useState('');
  const [selectedPolyclinicId, setSelectedPolyclinicId] = useState('');
  const [editingId, setEditingId] = useState(null);

  const fetchData = async () => {
    try {
      const [appts, pats, docs, depts, polys] = await Promise.all([
        AppointmentService.getAll(page),
        PatientService.getAll(0, 1000),
        DoctorService.getAll(0, 1000),
        DepartmentService.getAll(0, 1000),
        PolyclinicService.getAll()
      ]);
      setAppointments(appts.content || []);
      setTotalPages(appts.totalPages || 0);
      setPatients(pats.content || []);
      setDoctors(docs.content || []);
      setDepartments(depts.content || depts || []);
      setPolyclinics(polys || []);
    } catch (error) {
      toast.error('Veriler yüklenemedi.');
    }
  };

  useEffect(() => {
    fetchData();
  }, [page]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.patientId || !formData.doctorId || !formData.appointmentDate) {
      toast.error('Lütfen tüm zorunlu alanları doldurun.');
      return;
    }
    
    try {
      if (editingId) {
        await AppointmentService.update(editingId, formData);
      } else {
        await AppointmentService.create(formData);
      }
      setIsModalOpen(false);
      setEditingId(null);
      fetchData();
      toast.success(editingId ? 'Randevu başarıyla güncellendi.' : 'Randevu başarıyla oluşturuldu.');
    } catch (error) {
      toast.error(`İşlem başarısız oldu:\n${error.message}`);
    }
  };

  const handleEdit = (appt) => {
    setFormData({ 
      patientId: appt.patientId,
      doctorId: appt.doctorId,
      appointmentDate: appt.appointmentDate.slice(0, 16), // YYYY-MM-DDTHH:MM için kırp
      notes: appt.notes || ''
    });
    setEditingId(appt.id);
    setIsModalOpen(true);
  };

  const handleDelete = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeDelete = async () => {
    try {
      await AppointmentService.delete(confirmModal.id);
      fetchData();
      toast.success('Randevu başarıyla silindi.');
    } catch (error) {
      toast.error('Silme işlemi başarısız.');
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const handleStatusChange = async (id, newStatus) => {
    try {
      await AppointmentService.updateStatus(id, newStatus);
      fetchData();
      toast.success('Randevu durumu güncellendi.');
    } catch (error) {
      toast.error(`Durum güncellenemedi:\n${error.message}`);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      SCHEDULED: { label: t('status_scheduled'), color: '#3b82f6', bg: '#eff6ff' },
      ARRIVED: { label: t('status_arrived') || 'Hastanede', color: '#8b5cf6', bg: '#f5f3ff' },
      IN_EXAMINATION: { label: t('status_in_examination') || 'Muayenede', color: '#ec4899', bg: '#fdf2f8' },
      COMPLETED: { label: t('status_completed'), color: '#10b981', bg: '#ecfdf5' },
      CANCELLED: { label: t('status_cancelled'), color: '#ef4444', bg: '#fef2f2' },
      NO_SHOW: { label: t('status_no_show'), color: '#f59e0b', bg: '#fffbeb' }
    };
    
    const conf = statusConfig[status] || { label: status, color: '#64748b', bg: '#f1f5f9' };
    return (
      <span style={{ 
        display: 'inline-flex', alignItems: 'center', gap: '6px',
        padding: '4px 10px', borderRadius: '6px', fontSize: '0.65rem', 
        fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.5px',
        color: conf.color, backgroundColor: `${conf.color}15`, border: `1px solid ${conf.color}30`
      }}>
        <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: conf.color }}></span>
        {conf.label}
      </span>
    );
  };

  const columns = [
    { header: t('patient'), accessor: 'patientFullName' },
    { header: t('doctor'), render: (row) => `${row.doctorFullName} (${t(row.departmentName)})` },
    { header: t('date'), render: (row) => new Date(row.appointmentDate).toLocaleString('tr-TR') },
    { header: t('status'), render: (row) => getStatusBadge(row.status) }
  ];

  const renderActions = (row) => (
    <select 
      value={row.status} 
      onChange={(e) => handleStatusChange(row.id, e.target.value)}
      style={{ padding: '0.25rem', borderRadius: '4px', border: '1px solid #e2e8f0', fontSize: '0.75rem', marginRight: '5px', backgroundColor: 'var(--surface)', color: 'var(--text-main)' }}
    >
      <option value="SCHEDULED">{t('status_scheduled')}</option>
      <option value="ARRIVED">{t('status_arrived') || 'Hastanede'}</option>
      <option value="IN_EXAMINATION">{t('status_in_examination') || 'Muayenede'}</option>
      <option value="COMPLETED">{t('status_completed')}</option>
      <option value="CANCELLED">{t('status_cancelled')}</option>
      <option value="NO_SHOW">{t('status_no_show')}</option>
    </select>
  );

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>{t('appointments')}</h1>
        <button 
          className={styles.primaryBtn} 
          onClick={() => {
            setFormData({ patientId: '', doctorId: '', appointmentDate: '', notes: '' });
            setSelectedDepartmentId('');
            setSelectedPolyclinicId('');
            setEditingId(null);
            setIsModalOpen(true);
          }}
        >
          + {t('create_appointment')}
        </button>
      </div>

      <DataTable 
        columns={columns} 
        data={appointments} 
        onEdit={handleEdit} 
        onDelete={handleDelete}
        actions={renderActions}
        page={page}
        totalPages={totalPages}
        onPageChange={setPage}
      />

      {(() => {
        const filteredPolyclinics = selectedDepartmentId 
          ? polyclinics.filter(p => p.departmentId === parseInt(selectedDepartmentId))
          : [];
        const filteredDoctors = selectedPolyclinicId 
          ? doctors.filter(d => d.polyclinicId === parseInt(selectedPolyclinicId))
          : [];

        return (
          <Modal 
            isOpen={isModalOpen} 
            onClose={() => setIsModalOpen(false)} 
            title={editingId ? 'Randevuyu Düzenle' : 'Yeni Randevu Oluştur'}
          >
        <form onSubmit={handleSubmit}>
          
          <div className={styles.formGroup}>
            <label>Hasta</label>
            <select 
              required 
              value={formData.patientId} 
              onChange={(e) => setFormData({...formData, patientId: e.target.value})}
            >
              <option value="">-- Hasta Seçin --</option>
              {patients.map(pat => (
                <option key={pat.id} value={pat.id}>{pat.tcIdentityNumber} - {pat.firstName} {pat.lastName}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>Bölüm</label>
            <select 
              required 
              value={selectedDepartmentId} 
              onChange={(e) => {
                setSelectedDepartmentId(e.target.value);
                setSelectedPolyclinicId('');
                setFormData({...formData, doctorId: ''});
              }}
            >
              <option value="">-- Bölüm Seçin --</option>
              {departments.map(dept => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>Poliklinik</label>
            <select 
              required 
              disabled={!selectedDepartmentId}
              value={selectedPolyclinicId} 
              onChange={(e) => {
                setSelectedPolyclinicId(e.target.value);
                setFormData({...formData, doctorId: ''});
              }}
            >
              <option value="">-- Poliklinik Seçin --</option>
              {filteredPolyclinics.map(poly => (
                <option key={poly.id} value={poly.id}>{poly.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>Doktor</label>
            <select 
              required 
              disabled={!selectedPolyclinicId}
              value={formData.doctorId} 
              onChange={(e) => setFormData({...formData, doctorId: e.target.value})}
            >
              <option value="">-- Doktor Seçin --</option>
              {filteredDoctors.map(doc => (
                <option key={doc.id} value={doc.id}>{doc.specialization} {doc.firstName} {doc.lastName}</option>
              ))}
            </select>
          </div>
          
          <div className={styles.formGroup}>
            <label>Randevu Tarihi ve Saati</label>
            <input 
              type="datetime-local" 
              required 
              value={formData.appointmentDate} 
              onChange={(e) => setFormData({...formData, appointmentDate: e.target.value})} 
            />
          </div>
          
          <div className={styles.formGroup}>
            <label>Notlar (Şikayet vb.)</label>
            <textarea 
              rows="3"
              value={formData.notes} 
              onChange={(e) => setFormData({...formData, notes: e.target.value})} 
            />
          </div>
          
          <div className={styles.formActions}>
            <button type="button" className={styles.cancelBtn} onClick={() => setIsModalOpen(false)}>{t('cancel')}</button>
            <button type="submit" className={styles.primaryBtn}>{t('save')}</button>
          </div>
        </form>
      </Modal>
      );
      })()}

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title="Randevuyu Sil"
        message="Bu randevuyu iptal edip sistemden silmek istediğinize emin misiniz?"
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
      />
    </div>
  );
}
