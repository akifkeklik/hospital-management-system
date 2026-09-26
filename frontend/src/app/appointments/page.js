'use client';
import { useState, useEffect, useCallback, useRef } from 'react';
import { AppointmentService, PatientService, DoctorService, DepartmentService, PolyclinicService } from '../../services/api';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import ConfirmModal from '../../components/ConfirmModal';
import AsyncSelect from '../../components/AsyncSelect';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import { useAuth } from '../../context/AuthContext';
import styles from '../shared.module.css';

export default function AppointmentsPage() {
  const { t } = useSettings();
  const { user, role } = useAuth();

  const [appointments, setAppointments] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [polyclinics, setPolyclinics] = useState([]);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });

  const [formData, setFormData] = useState({
    patientId: '', doctorId: '', appointmentDate: '', notes: ''
  });
  const [initialPatientLabel, setInitialPatientLabel] = useState('');
  const [initialDoctorLabel, setInitialDoctorLabel] = useState('');

  const [selectedDepartmentId, setSelectedDepartmentId] = useState('');
  const [selectedPolyclinicId, setSelectedPolyclinicId] = useState('');
  const [editingId, setEditingId] = useState(null);


  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const triggerRefresh = () => setRefreshTrigger(prev => prev + 1);

  useEffect(() => {
    let ignore = false;
    async function loadData() {
      try {
        let appts;
        if (role === 'ROLE_PATIENT') {
          appts = await AppointmentService.getByPatient(user?.id, page, pageSize);
        } else if (role === 'ROLE_DOCTOR') {
          appts = await AppointmentService.getByDoctor(user?.id, page, pageSize);
        } else {
          appts = await AppointmentService.getAll(page, pageSize);
        }
        if (!ignore) {
          setAppointments(appts.items || []);
          setTotalPages(appts.totalPages || 0);
        }
      } catch (error) {
        if (!ignore) {
          toast.error(t('error_loading_data'));
        }
      }
    }

    if (user?.id || role === 'ROLE_ADMIN') {
      loadData();
    }
    return () => { ignore = true; };
  }, [role, user?.id, page, pageSize, t, refreshTrigger]);

  // Sadece modal açıldığında departmanları yükle (bulk fetch iptal edildi)
  useEffect(() => {
    if (isModalOpen && departments.length === 0) {
      DepartmentService.getAll(0, 100).then(res => setDepartments(res.content || res || []));
    }
  }, [isModalOpen, departments.length]);

  // Departman seçildiğinde poliklinikleri getir
  useEffect(() => {
    if (selectedDepartmentId) {
      PolyclinicService.getByDepartmentId(selectedDepartmentId).then(res => setPolyclinics(res || []));
    }
  }, [selectedDepartmentId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.patientId || !formData.doctorId || !formData.appointmentDate) {
      toast.error(t('fill_required_fields'));
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
      triggerRefresh();
      toast.success(editingId ? t('appointment_updated') : t('appointment_created'));
    } catch (error) {
      toast.error(`${t('operation_failed')}:\n${error.message}`);
    }
  };

  const handleEdit = (appt) => {
    setFormData({
      patientId: appt.patientId,
      doctorId: appt.doctorId,
      appointmentDate: appt.appointmentDate.slice(0, 16),
      notes: appt.notes || ''
    });
    setInitialPatientLabel(appt.patientFullName);
    setInitialDoctorLabel(`${appt.doctorFullName} (${t(appt.departmentName)})`);
    setSelectedDepartmentId(''); // Poliklinik ve departman API yapısına göre otomatik seçmek karmaşık olabilir.
    setSelectedPolyclinicId('');
    setEditingId(appt.id);
    setIsModalOpen(true);
  };

  const handleDelete = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeDelete = async () => {
    try {
      await AppointmentService.delete(confirmModal.id);
      triggerRefresh();
      toast.success(t('appointment_deleted'));
    } catch (error) {
      toast.error(t('delete_failed'));
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const handleStatusChange = async (id, newStatus) => {
    try {
      await AppointmentService.updateStatus(id, newStatus);
      triggerRefresh();
      toast.success(t('status_updated'));
    } catch (error) {
      toast.error(`${t('status_update_failed')}:\n${error.message}`);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      SCHEDULED: { label: t('status_scheduled'), color: '#3b82f6', bg: '#eff6ff' },
      ARRIVED: { label: t('status_arrived'), color: '#8b5cf6', bg: '#f5f3ff' },
      IN_EXAMINATION: { label: t('status_in_examination'), color: '#ec4899', bg: '#fdf2f8' },
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
      <option value="ARRIVED">{t('status_arrived')}</option>
      <option value="IN_EXAMINATION">{t('status_in_examination')}</option>
      <option value="COMPLETED">{t('status_completed')}</option>
      <option value="CANCELLED">{t('status_cancelled')}</option>
      <option value="NO_SHOW">{t('status_no_show')}</option>
    </select>
  );

  const loadPatients = async (query) => {
    if (!query || query.length < 2) return [];
    try {
      const res = await PatientService.search(query, 0, 10);
      const items = res.content || [];
      return items.map(p => ({ value: p.id, label: `${p.tcIdentityNumber} - ${p.firstName} ${p.lastName}` }));
    } catch (e) {
      return [];
    }
  };

  const loadDoctors = async (query) => {
    if (!query || query.length < 2) return [];
    try {
      const res = await DoctorService.search(query, 0, 10);
      let items = res.content || [];
      if (selectedPolyclinicId) {
        items = items.filter(d => d.polyclinicId === parseInt(selectedPolyclinicId));
      }
      return items.map(d => ({ value: d.id, label: `${d.specialization} ${d.firstName} ${d.lastName}` }));
    } catch (e) {
      return [];
    }
  };

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>{t('appointments')}</h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', backgroundColor: 'var(--surface)', padding: '0.2rem 0.5rem 0.2rem 1rem', borderRadius: '8px', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>Kayıt Sayısı:</span>
            <select 
              value={pageSize} 
              onChange={(e) => { setPageSize(Number(e.target.value)); setPage(0); }}
              style={{ width: 'auto', padding: '0.4rem 2rem 0.4rem 0.8rem', border: 'none', backgroundColor: 'transparent', boxShadow: 'none', fontWeight: '600', color: 'var(--primary)' }}
            >
              <option value={3}>3</option>
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>
          <button
          className={styles.primaryBtn}
          onClick={() => {
            let initialPatient = '';
            let initialPatientId = '';
            if (role === 'ROLE_PATIENT') {
              initialPatientId = user?.id || '';
              initialPatient = `${user?.firstName} ${user?.lastName}`;
            }

            let initialDoctor = '';
            let initialDoctorId = '';
            if (role === 'ROLE_DOCTOR') {
              initialDoctorId = user?.id || '';
              initialDoctor = `${user?.firstName} ${user?.lastName}`;
            }

            setFormData({
              patientId: initialPatientId,
              doctorId: initialDoctorId,
              appointmentDate: '',
              notes: ''
            });
            setInitialPatientLabel(initialPatient);
            setInitialDoctorLabel(initialDoctor);
            setSelectedDepartmentId('');
            setSelectedPolyclinicId('');
            setEditingId(null);
            setIsModalOpen(true);
          }}
        >
          + {t('create_appointment')}
        </button>
        </div>
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

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingId ? t('edit_appointment') : t('create_appointment')}
      >
        <form onSubmit={handleSubmit}>

          <div className={styles.formGroup}>
            <label>{t('patient')}</label>
            {role === 'ROLE_PATIENT' ? (
              <select disabled required value={formData.patientId} onChange={() => {}}>
                <option value={formData.patientId}>{initialPatientLabel || `${user?.firstName} ${user?.lastName}`}</option>
              </select>
            ) : (
              <AsyncSelect
                value={formData.patientId}
                initialLabel={initialPatientLabel}
                onChange={(val) => setFormData({...formData, patientId: val})}
                loadOptions={loadPatients}
                placeholder={t('select_patient')}
              />
            )}
          </div>

          <div className={styles.formGroup}>
            <label>{t('department')}</label>
            <select
              value={selectedDepartmentId}
              onChange={(e) => {
                setSelectedDepartmentId(e.target.value);
                setSelectedPolyclinicId('');
                setPolyclinics([]);
                if (role !== 'ROLE_DOCTOR') setFormData({...formData, doctorId: ''});
              }}
            >
              <option value="">-- {t('select_department')} --</option>
              {departments.map(dept => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>{t('polyclinics')}</label>
            <select
              disabled={!selectedDepartmentId}
              value={selectedPolyclinicId}
              onChange={(e) => {
                setSelectedPolyclinicId(e.target.value);
                if (role !== 'ROLE_DOCTOR') setFormData({...formData, doctorId: ''});
              }}
            >
              <option value="">-- {t('select_polyclinic')} --</option>
              {polyclinics.map(poly => (
                <option key={poly.id} value={poly.id}>{poly.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>{t('doctor')}</label>
            {role === 'ROLE_DOCTOR' ? (
              <select disabled required value={formData.doctorId} onChange={() => {}}>
                <option value={formData.doctorId}>{initialDoctorLabel || `${user?.firstName} ${user?.lastName}`}</option>
              </select>
            ) : (
              <AsyncSelect
                value={formData.doctorId}
                initialLabel={initialDoctorLabel}
                onChange={(val) => setFormData({...formData, doctorId: val})}
                loadOptions={loadDoctors}
                placeholder={t('select_doctor')}
                disabled={!selectedPolyclinicId}
              />
            )}
          </div>

          <div className={styles.formGroup}>
            <label>{t('date_and_time')}</label>
            <input
              type="datetime-local"
              required
              value={formData.appointmentDate}
              onChange={(e) => setFormData({...formData, appointmentDate: e.target.value})}
            />
          </div>

          <div className={styles.formGroup}>
            <label>{t('appointment_notes')}</label>
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

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={t('delete_appointment_title')}
        message={t('delete_appointment_confirm')}
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
      />
    </div>
  );
}
