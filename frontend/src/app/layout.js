import { Inter } from 'next/font/google';
import './globals.css';
import ClientLayout from './ClientLayout';

const inter = Inter({ 
  subsets: ['latin'],
  display: 'swap',
  variable: '--font-inter',
});

export const metadata = {
  title: 'Hastane Randevu Sistemi',
  description: 'DDD Mimari ile geliştirilmiş Hastane Randevu Sistemi',
  manifest: '/manifest.json',
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
        <ClientLayout>
          {children}
        </ClientLayout>
      </body>
    </html>
  );
}
