import { Inter } from 'next/font/google';
import './globals.css';
import ClientLayout from './ClientLayout';
import { AuthProvider } from '../context/AuthContext';
import { SettingsProvider } from '../context/SettingsContext';

const inter = Inter({ 
  subsets: ['latin'],
  display: 'swap',
  variable: '--font-inter',
});

export const metadata = {
  title: 'Hastane Randevu Sistemi',
  description: 'Hastane randevu, muayene ve doktor yönetimi: hasta, doktor ve yönetici panelleri.',
  manifest: '/manifest.json',
  icons: { icon: '/favicon.ico', apple: '/icons/apple-touch-icon.png' },
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'HBYS',
  },
};

export const viewport = {
  themeColor: '#818cf8',
};

export default function RootLayout({ children }) {
  return (
    <html lang="tr" className={inter.variable}>
      <body className={inter.className}>
        <SettingsProvider>
          <AuthProvider>
            <ClientLayout>
              {children}
            </ClientLayout>
          </AuthProvider>
        </SettingsProvider>
      </body>
    </html>
  );
}
