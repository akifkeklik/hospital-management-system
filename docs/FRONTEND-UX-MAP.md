# FRONTEND UX MAP

## 1. USER ROLES & CORE FLOWS

### Patient
- **Login**: `app/login/page.js` -> Inputs email/password -> Context sets role -> Redirect to `/`.
- **Dashboard**: `app/page.js` (Role-based redirect to `PatientDashboard.js`). Shows stats, `HospitalMap`, `SymptomAnalyzer`.
- **Appointment Creation**: `app/book-appointment/page.js` -> Selects Department -> Selects Doctor -> Picks slot -> Confirm -> Redirect.
- **Records/Appointments**: `app/patient-records/page.js` (Paginated list of completed apps), `app/appointments/page.js`.
- **Notifications**: `app/patient-notifications/page.js`.
- **Settings**: `app/settings/page.js`.

### Doctor
- **Login**: (Same page) -> Redirect to `/`.
- **Dashboard**: `DoctorDashboard.js`. Overview of today's stats, upcoming patient.
- **Queue**: `app/doctor/queue/page.js`. Real-time list of today's patients. Status transitions (SCHEDULED -> ARRIVED -> IN_EXAMINATION -> COMPLETED).
- **Leaves**: `app/doctor/leaves/page.js`. Request leave, view history.
- **Notifications**: Alert toast.

### Admin
- **Login**: (Same page) -> Redirect to `/`.
- **Doctor Requests**: `app/admin/doctor-requests/page.js`. Approve new doctors.
- **Doctor Leaves**: `app/admin/doctor-leaves/page.js`. Approve/reject leaves.
- **Departments/Polyclinics**: `app/departments/page.js`, `app/admin/polyclinics/page.js`.

## 2. UX AUDIT FINDINGS

### Empty States
- Custom `EmptyState.js` component exists. Shows an icon and a message. Good UX.
- Loading States: Handled via `LoadingScreen.js` for full-page, or inline `isLoading` states in buttons.
- Forms: Destructive actions use `ConfirmModal.js` instead of native `window.confirm`.

### Pagination & Async States
- Handled via `Pagination.js`. Flickering issues were resolved by backend pagination, but rapid clicks could still trigger race conditions (stale data) since React `useEffect` lacks abort controllers.

## 3. UI AUDIT & DESIGN SYSTEM

### Global CSS
- `globals.css` implements a design system using native CSS variables (`--primary`, `--surface`, `--text-main`, `--shadow-md`).
- Dark mode is default, light mode toggled via `[data-theme='light']`.
- Premium feel with `color-mix()` for tinted surfaces.

### UI Inconsistencies
- Components are heavily modularized using `.module.css`.
- Duplicate dashboard layouts: `PatientDashboard.module.css` vs `DoctorDashboard.module.css` both implement similar hero banners and container structures with slightly different styles.
- Headers: `Header.js`, `PatientHeader.js`, and `DoctorHeader.js` indicate a lack of a unified flexible Header component.

## 4. ACCESSIBILITY (A11y)
- Buttons use proper classes, but ARIA labels might be missing on complex interactive elements like the `HospitalMap.js`.
- High contrast themes and Text-to-Speech were recently added in the git history (`feat(accessibility)`).

## 5. RESPONSIVE DESIGN
- Handled manually in each `.module.css` via media queries (e.g., `@media (max-width: 768px)`).
- `Sidebar.js` handles mobile view by converting to a bottom nav or collapsible menu (need to verify exact behavior, but standard SaaS pattern).

## 6. CURRENT TECHNICAL DEBT
- **Duplicate Components**: `Header.js` vs role-specific headers.
- **Missing Hooks**: API calls are often made directly in `useEffect` rather than using custom hooks (e.g., `useAppointments`), leading to duplicated `try/catch` and `setLoading` boilerplate across pages.
- **API Logic in Components**: Components directly construct URLs instead of completely relying on `api.js` boundaries.
