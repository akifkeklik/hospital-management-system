'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { DepartmentService, PatientService, DoctorService, AppointmentService } from '../services/api';
import DashboardCharts from '../components/DashboardCharts';
import PatientDashboard from '../components/PatientDashboard';
import DoctorDashboard from '../components/DoctorDashboard';
import { useSettings } from '../context/SettingsContext';
import styles from './page.module.css';

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
}

export default function Dashboard() {
  const { t } = useSettings();
  const [stats, setStats] = useState({
    departments: 0,
    patients: 0,
    doctors: 0,
    appointments: 0
  });
  const [chartData, setChartData] = useState({
    departments: [],
    doctors: [],
    appointments: []
  });
  const [loading, setLoading] = useState(true);
  const [role, setRole] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      const decoded = parseJwt(token);
      if (decoded && decoded.role) {
        setRole(decoded.role);
        if (decoded.role === 'ROLE_PATIENT' || decoded.role === 'ROLE_DOCTOR' || decoded.role === 'HEKIM' || decoded.role === 'ROLE_HEKIM' || decoded.role === 'HASTA' || decoded.role === 'ROLE_HASTA') {
          // Hasta veya Doktor ise genel istatistik çekmeye gerek yok, kendi dashboard'ları var
          setLoading(false);
          return;
        }
      }
    }

    async function fetchStats() {
      try {
        const [depts, pats, docs, appts] = await Promise.all([
          DepartmentService.getAll(0, 1000),
          PatientService.getAll(0, 1000),
          DoctorService.getAll(0, 1000),
          AppointmentService.getAll(0, 1000)
        ]);
        
        setStats({
          departments: depts.totalElements || 0,
          patients: pats.totalElements || 0,
          doctors: docs.totalElements || 0,
          appointments: appts.totalElements || 0
        });

        setChartData({
          departments: depts.content || [],
          doctors: docs.content || [],
          appointments: appts.content || []
        });
      } catch (error) {
        console.error("Dashboard istatistikleri alınamadı", error);
      } finally {
        setLoading(false);
      }
    }
    
    fetchStats();
  }, []);

  if (role === 'ROLE_PATIENT' || role === 'PATIENT' || role === 'HASTA' || role === 'ROLE_HASTA') {
    return <PatientDashboard />;
  }

  if (role === 'ROLE_DOCTOR' || role === 'DOCTOR' || role === 'HEKIM' || role === 'ROLE_HEKIM') {
    return <DoctorDashboard />;
  }

  return (
    <div className={styles.dashboard}>
      <h1 className={styles.title}>{t('welcome')}</h1>
      <p className={styles.subtitle}>{t('welcome_sub')}</p>
      
      {loading ? (
        <p>{t('loading')}</p>
      ) : (
        <div className={styles.grid}>
          <Link href="/departments" className={styles.card}>
            <div className={styles.cardIcon}>🏢</div>
            <div className={styles.cardInfo}>
              <h3>{t('departments')}</h3>
              <p className={styles.count}>{stats.departments}</p>
            </div>
          </Link>
          
          <Link href="/patients" className={styles.card}>
            <div className={styles.cardIcon}>🧑</div>
            <div className={styles.cardInfo}>
              <h3>{t('registered_patients')}</h3>
              <p className={styles.count}>{stats.patients}</p>
            </div>
          </Link>
          
          <Link href="/doctors" className={styles.card}>
            <div className={styles.cardIcon}>👨‍⚕️</div>
            <div className={styles.cardInfo}>
              <h3>{t('doctors')}</h3>
              <p className={styles.count}>{stats.doctors}</p>
            </div>
          </Link>
          
          <Link href="/appointments" className={styles.card}>
            <div className={styles.cardIcon}>📅</div>
            <div className={styles.cardInfo}>
              <h3>{t('total_appointments')}</h3>
              <p className={styles.count}>{stats.appointments}</p>
            </div>
          </Link>
        </div>
      )}

      {!loading && (
        <DashboardCharts 
          departments={chartData.departments}
          doctors={chartData.doctors}
          appointments={chartData.appointments}
        />
      )}
    </div>
  );
}
