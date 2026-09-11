-- 1. system_settings
CREATE TABLE system_settings (
    id BIGINT PRIMARY KEY,
    appointment_duration INT NOT NULL,
    work_start_time VARCHAR(255) NOT NULL,
    work_end_time VARCHAR(255) NOT NULL,
    lunch_break_start VARCHAR(255) NOT NULL,
    lunch_break_end VARCHAR(255) NOT NULL,
    maintenance_mode BOOLEAN NOT NULL
);

-- 2. departments
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN
);

-- 3. patients
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    tc_identity_number VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    email VARCHAR(100),
    is_active BOOLEAN
);

-- 4. users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    reference_id BIGINT,
    needs_password_change BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. polyclinics
CREATE TABLE polyclinics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    room_number VARCHAR(255),
    department_id BIGINT NOT NULL,
    CONSTRAINT fk_polyclinics_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 6. doctors
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    tc_identity_number VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    email VARCHAR(100),
    is_active BOOLEAN,
    department_id BIGINT NOT NULL,
    polyclinic_id BIGINT,
    CONSTRAINT fk_doctors_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_doctors_polyclinic FOREIGN KEY (polyclinic_id) REFERENCES polyclinics(id)
);

-- 7. doctor_registration_requests
CREATE TABLE doctor_registration_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tc_identity_number VARCHAR(11) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    specialization VARCHAR(255),
    department_id BIGINT,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    request_date DATETIME(6) NOT NULL,
    CONSTRAINT fk_doc_reg_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 8. doctor_leaves
CREATE TABLE doctor_leaves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_doctor_leaves_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE INDEX IDX_DOCTOR_LEAVE_DOC_DATES ON doctor_leaves (doctor_id, start_date, end_date);

-- 9. notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT,
    doctor_id BIGINT,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_notifications_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_notifications_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE INDEX IDX_NOTIFICATION_PATIENT_DATE ON notifications (patient_id, created_at DESC);
CREATE INDEX IDX_NOTIFICATION_DOCTOR_DATE ON notifications (doctor_id, created_at DESC);

-- 10. appointments
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date DATETIME(6) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes VARCHAR(500),
    is_active BOOLEAN,
    active_slot_id VARCHAR(100) UNIQUE,
    version BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE INDEX IDX_APPOINTMENT_DOC_STAT_DATE ON appointments (doctor_id, status, appointment_date);

-- 11. diagnoses
CREATE TABLE diagnoses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    icd10_code VARCHAR(10) NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT fk_diagnoses_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- 12. prescriptions
CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    medication_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    usage_instruction VARCHAR(255) NOT NULL,
    CONSTRAINT fk_prescriptions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
