'use client';
import { usePathname } from 'next/navigation';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PatientHeader from '../components/PatientHeader';
import DoctorHeader from '../components/DoctorHeader';

import ToastContainer from '../components/Toast';
import { useAuth } from '../context/AuthContext';

export default function ClientLayout({ children }) {
  const pathname = usePathname();
  const isAuthPage = pathname === '/login' || pathname === '/register' || pathname === '/forgot-password';
  const { role } = useAuth();

  // Eğer sayfa login veya register ise Sidebar ve Header'ı KESİNLİKLE GİZLE!
  if (isAuthPage) {
    return (
      <>
        {children}
        <ToastContainer />
      </>
    );
  }

  // Hasta veya Doktor ise özel (Sidebar'sız) layout
  if (role === 'ROLE_PATIENT' || role === 'ROLE_DOCTOR') {
    return (
      <>
        <div className="app-container" style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
          {role === 'ROLE_PATIENT' ? <PatientHeader /> : null}
          {role === 'ROLE_DOCTOR' ? <DoctorHeader /> : null}
          
          <main className="patient-page-content" style={{ flex: 1, overflowY: 'auto', padding: '2rem', display: 'flex', flexDirection: 'column', minHeight: 0 }}>
            <div style={{ maxWidth: '1200px', margin: '0 auto', width: '100%', display: 'flex', flexDirection: 'column', flex: 1, minHeight: 0 }}>
              {children}
            </div>
          </main>
        </div>
        <ToastContainer />
      </>
    );
  }

  // Sadece Admin (ROLE_ADMIN) veya diğer rollere Sidebar'lı Layout
  return (
    <>
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Header />
          <main className="page-content">
            {children}
          </main>
        </div>
      </div>
      <ToastContainer />
    </>
  );
}
