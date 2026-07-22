'use client';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PatientHeader from '../components/PatientHeader';
import DoctorHeader from '../components/DoctorHeader';
import { SettingsProvider } from '../context/SettingsContext';
import ToastContainer from '../components/Toast';

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
}

export default function ClientLayout({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const isAuthPage = pathname === '/login' || pathname === '/register' || pathname === '/forgot-password';
  const [mounted, setMounted] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [role, setRole] = useState(null);

  useEffect(() => {
    setMounted(true);
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    
    if (!token && !isAuthPage) {
      router.push('/login');
    } else if (token) {
      const decoded = parseJwt(token);
      if (decoded && decoded.role) {
        setRole(decoded.role);
      }
      setIsAuthenticated(true);
    }
  }, [pathname, isAuthPage, router]);

  // Next.js hydration uyumsuzluklarını önlemek için mount olana kadar boş dönebiliriz.
  if (!mounted) return null;

  // Eğer sayfa login veya register ise Sidebar ve Header'ı KESİNLİKLE GİZLE!
  if (isAuthPage) {
    return (
      <SettingsProvider>
        {children}
        <ToastContainer />
      </SettingsProvider>
    );
  }

  // Token yoksa (henüz redirect olmadıysa) arayüzü çizme ki ekran saniyelik gözükmesin
  if (!isAuthenticated && !isAuthPage) return null;

  // Hasta veya Doktor ise özel (Sidebar'sız) layout
  if (role === 'ROLE_PATIENT' || role === 'ROLE_DOCTOR') {
    return (
      <SettingsProvider>
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
      </SettingsProvider>
    );
  }

  // Sadece Admin (ROLE_ADMIN) veya diğer rollere Sidebar'lı Layout
  return (
    <SettingsProvider>
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
    </SettingsProvider>
  );
}
