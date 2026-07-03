const rawUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const API_BASE_URL = rawUrl.endsWith('/api') ? rawUrl : `${rawUrl}/api`;

/**
 * 🌐 Ortak API İsteği Yapan Fonksiyon
 * 
 * Neden bunu kullanıyoruz?
 * - Her sayfada uzun uzun fetch, then, catch yazmamak için.
 * - Hataları tek bir yerden yönetmek için.
 * - JSON dönüşümlerini otomatik yapmak için.
 */
async function fetchAPI(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };

  // Artık Token'ı Cookie'den alacağımız için Authorization header'a gerek yok.
  // Geriye dönük uyumluluk için localStorage'da kaldıysa temizlenecek (logout aşamasında)

  const config = {
    ...options,
    credentials: 'include', // Cookie'leri backend'e gönder!
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);

    // Eğer başarılı değilse hata fırlat
    if (!response.ok) {
      if (response.status === 401 || response.status === 403 || (response.status === 500 && url.includes('/me'))) {
        if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
          localStorage.removeItem('token');
          window.location.href = '/login';
          return new Promise(() => {}); // Redirect esnasında hata fırlatmayı engellemek için askıda bırak
        }
      }

      let errorMessage = response.statusText;
      try {
        const errorData = await response.json();
        if (errorData.details) {
          // Validation error
          const detailsStr = Object.values(errorData.details).join('\n');
          errorMessage = `${errorData.error}:\n${detailsStr}`;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        }
      } catch (e) {
        // Not JSON, just use raw text
        const rawText = await response.text();
        if (rawText) errorMessage = rawText;
      }
      throw new Error(errorMessage);
    }

    // 204 No Content ise boş dön (Örn: DELETE işleminde)
    if (response.status === 204) {
      return null;
    }

    return await response.json();
  } catch (error) {
    // console.error(`Fetch error on ${url}:`, error); // Removed to prevent Next.js overlay
    throw error;
  }
}

// ── BÖLÜM (DEPARTMENT) API ──
export const DepartmentService = {
  getAll: (page = 0, size = 5) => fetchAPI(`/departments?page=${page}&size=${size}`),
  getById: (id) => fetchAPI(`/departments/${id}`),
  create: (data) => fetchAPI('/departments', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/departments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => fetchAPI(`/departments/${id}`, { method: 'DELETE' }),
};

// ── HASTA (PATIENT) API ──
export const PatientService = {
  getAll: (page = 0, size = 5) => fetchAPI(`/patients?page=${page}&size=${size}`),
  getById: (id) => fetchAPI(`/patients/${id}`),
  create: (data) => fetchAPI('/patients', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/patients/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => fetchAPI(`/patients/${id}`, { method: 'DELETE' }),
};

// ── DOKTOR (DOCTOR) API ──
export const DoctorService = {
  getAll: (page = 0, size = 5) => fetchAPI(`/doctors?page=${page}&size=${size}`),
  getById: (id) => fetchAPI(`/doctors/${id}`),
  create: (data) => fetchAPI('/doctors', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/doctors/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => fetchAPI(`/doctors/${id}`, { method: 'DELETE' }),
};

// ── RANDEVU (APPOINTMENT) API ──
export const AppointmentService = {
  getAll: (page = 0, size = 5) => fetchAPI(`/appointments?page=${page}&size=${size}`),
  getById: (id) => fetchAPI(`/appointments/${id}`),
  getByPatient: (patientId) => fetchAPI(`/appointments/patient/${patientId}`),
  getByDoctor: (doctorId) => fetchAPI(`/appointments/doctor/${doctorId}`),
  create: (data) => fetchAPI('/appointments', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/appointments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  updateStatus: (id, status) => fetchAPI(`/appointments/${id}/status`, { 
    method: 'PATCH', 
    body: JSON.stringify({ status }) 
  }),
  delete: (id) => fetchAPI(`/appointments/${id}`, { method: 'DELETE' }),
};

// ── AUTHENTICATION API ──
export const AuthService = {
  login: (username, password) => fetchAPI('/auth/login', { 
    method: 'POST', 
    body: JSON.stringify({ username, password }) 
  }),
  register: (data) => fetchAPI('/auth/register', { 
    method: 'POST', 
    body: JSON.stringify(data) 
  }),
  resetPassword: (tcIdentityNumber, email, newPassword) => fetchAPI('/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify({ tcIdentityNumber, email, newPassword })
  }),
  forceChangePassword: (tcIdentityNumber, newPassword) => fetchAPI('/auth/force-change-password', {
    method: 'POST',
    body: JSON.stringify({ tcIdentityNumber, newPassword })
  }),
  getMe: () => fetchAPI('/auth/me'),
};

// ── SYSTEM SETTINGS API ──
export const SystemSettingService = {
  getSettings: () => fetchAPI('/admin/settings'),
  updateSettings: (data) => fetchAPI('/admin/settings', {
    method: 'PUT',
    body: JSON.stringify(data)
  })
};

// ── DOCTOR LEAVES API ──
export const DoctorLeaveService = {
  getAll: () => fetchAPI('/doctor-leaves'),
  getByDoctorId: (doctorId) => fetchAPI(`/doctor-leaves/doctor/${doctorId}`),
  create: (data) => fetchAPI('/doctor-leaves', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  updateStatus: (id, status) => fetchAPI(`/doctor-leaves/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  }),
  delete: (id) => fetchAPI(`/doctor-leaves/${id}`, {
    method: 'DELETE'
  })
};

// ── NOTIFICATION API ──
export const NotificationService = {
  getByPatient: (patientId) => fetchAPI(`/notifications/patient/${patientId}`),
  getUnreadCountByPatient: (patientId) => fetchAPI(`/notifications/patient/${patientId}/unread-count`),
  getByDoctor: (doctorId) => fetchAPI(`/notifications/doctor/${doctorId}`),
  getUnreadCountByDoctor: (doctorId) => fetchAPI(`/notifications/doctor/${doctorId}/unread-count`),
  broadcastToDoctors: (message) => fetchAPI('/notifications/broadcast', {
    method: 'POST',
    body: message
  }),
  markAsRead: (id) => fetchAPI(`/notifications/${id}/read`, { method: 'PATCH' })
};

// ── POLYCLINIC API ──
export const PolyclinicService = {
  getAll: () => fetchAPI('/admin/polyclinics'),
  getByDepartmentId: (departmentId) => fetchAPI(`/admin/polyclinics/department/${departmentId}`),
  create: (data) => fetchAPI('/admin/polyclinics', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  update: (id, data) => fetchAPI(`/admin/polyclinics/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  }),
  delete: (id) => fetchAPI(`/admin/polyclinics/${id}`, {
    method: 'DELETE'
  })
};

// ── EXAMINATION API ──
export const ExaminationService = {
  addDiagnosis: (data) => fetchAPI('/examinations/diagnoses', { method: 'POST', body: JSON.stringify(data) }),
  getDiagnoses: (appointmentId) => fetchAPI(`/examinations/appointments/${appointmentId}/diagnoses`),
  deleteDiagnosis: (id) => fetchAPI(`/examinations/diagnoses/${id}`, { method: 'DELETE' }),
  
  addPrescription: (data) => fetchAPI('/examinations/prescriptions', { method: 'POST', body: JSON.stringify(data) }),
  getPrescriptions: (appointmentId) => fetchAPI(`/examinations/appointments/${appointmentId}/prescriptions`),
  deletePrescription: (id) => fetchAPI(`/examinations/prescriptions/${id}`, { method: 'DELETE' })
};
