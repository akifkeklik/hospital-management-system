'use client';
import {
  PieChart, Pie, Cell, Tooltip as PieTooltip, Legend as PieLegend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as BarTooltip, Legend as BarLegend,
  ResponsiveContainer
} from 'recharts';
import { useSettings } from '../context/SettingsContext';
import styles from './DashboardCharts.module.css';

const COLORS = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'];

export default function DashboardCharts({ doctorDistribution, appointmentsByDate }) {
  const { t } = useSettings();
  
  // 1. Veri Hazırlığı: Bölümlere Göre Doktor Sayısı
  const doctorDistArray = Object.keys(doctorDistribution || {}).map(deptName => {
    return { name: t(deptName), value: doctorDistribution[deptName] };
  }).filter(item => item.value > 0);

  // 2. Veri Hazırlığı: Günlük Randevu Yoğunluğu
  const appointmentData = Object.keys(appointmentsByDate || {})
    .sort() // Tarihe göre sırala
    .map(date => ({
      date,
      [t('appointments')]: appointmentsByDate[date]
    }))
    .slice(-7); // Son 7 günü göster

  return (
    <div className={styles.chartsContainer}>
      
      <div className={styles.chartBox}>
        <h3 className={styles.chartTitle}>{t('chart_dept_dist')}</h3>
        {doctorDistArray.length === 0 ? (
          <p className={styles.noData}>{t('no_data')}</p>
        ) : (
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie
                data={doctorDistArray}
                cx="50%"
                cy="50%"
                labelLine={false}
                outerRadius={60}
                fill="#8884d8"
                dataKey="value"
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
              >
                {doctorDistArray.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <PieTooltip />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className={styles.chartBox}>
        <h3 className={styles.chartTitle}>{t('chart_appt_density')}</h3>
        {appointmentData.length === 0 ? (
          <p className={styles.noData}>{t('no_data')}</p>
        ) : (
          <ResponsiveContainer width="100%" height={240}>
            <BarChart
              data={appointmentData}
              margin={{ top: 20, right: 30, left: 0, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" />
              <YAxis allowDecimals={false} />
              <BarTooltip />
              <BarLegend />
              <Bar dataKey={t('appointments')} fill="#2563eb" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

    </div>
  );
}
