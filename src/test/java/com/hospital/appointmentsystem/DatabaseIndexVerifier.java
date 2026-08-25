package com.hospital.appointmentsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * P1 Database Index Verification — Full audit of MySQL indexes.
 */
@SpringBootTest
public class DatabaseIndexVerifier {

    @Autowired
    private DataSource dataSource;

    @Test
    public void fullIndexAudit() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // STEP 1: MySQL Version
            System.out.println("=== STEP 1: MySQL Version ===");
            printQuery(stmt, "SELECT VERSION() AS mysql_version");

            // STEP 2: Current Indexes on Target Tables
            System.out.println("\n=== STEP 2: Current Indexes ===");
            String[] tables = {"notifications", "appointments", "doctor_leaves", "users", "patients", "doctors"};
            for (String table : tables) {
                System.out.println("\n--- INDEXES ON: " + table + " ---");
                printQuery(stmt, "SHOW INDEX FROM " + table);
            }

            // STEP 3: EXPLAIN on Critical Queries
            System.out.println("\n=== STEP 3: EXPLAIN Queries ===");

            String[][] explainQueries = {
                {"Notif_Patient_OrderBy", "EXPLAIN SELECT * FROM notifications WHERE patient_id = 1 ORDER BY created_at DESC"},
                {"Notif_Patient_Unread",  "EXPLAIN SELECT * FROM notifications WHERE patient_id = 1 AND is_read = false"},
                {"Notif_Doctor_OrderBy",  "EXPLAIN SELECT * FROM notifications WHERE doctor_id = 1 ORDER BY created_at DESC"},
                {"Notif_Doctor_Unread",   "EXPLAIN SELECT * FROM notifications WHERE doctor_id = 1 AND is_read = false"},
                {"Appt_Doc_Stat_Date",    "EXPLAIN SELECT * FROM appointments WHERE doctor_id = 1 AND status = 'SCHEDULED' AND appointment_date BETWEEN '2026-08-01' AND '2026-08-30'"},
                {"Appt_ByDoctor",         "EXPLAIN SELECT * FROM appointments WHERE doctor_id = 1"},
                {"Appt_ByPatient",        "EXPLAIN SELECT * FROM appointments WHERE patient_id = 1"},
                {"DLeave_ByDoctor",       "EXPLAIN SELECT * FROM doctor_leaves WHERE doctor_id = 1"},
                {"DLeave_IsOnLeave",      "EXPLAIN SELECT * FROM doctor_leaves WHERE doctor_id = 1 AND '2026-08-25' BETWEEN start_date AND end_date"},
                {"User_ByUsername",        "EXPLAIN SELECT * FROM users WHERE username = 'admin'"},
                {"User_ByEmail",          "EXPLAIN SELECT * FROM users WHERE email = 'admin@hospital.com'"},
                {"Patient_ByTC",          "EXPLAIN SELECT * FROM patients WHERE tc_identity_number = '12345678901'"},
            };

            for (String[] qp : explainQueries) {
                System.out.println("\n--- " + qp[0] + " ---");
                printQuery(stmt, qp[1]);
            }

            // STEP 4: DESC vs ASC — check if DESC index actually eliminates filesort
            System.out.println("\n=== STEP 4: DESC vs ASC check ===");
            // The existing index is (patient_id, created_at DESC)
            // Test: ORDER BY created_at DESC should NOT use filesort
            System.out.println("--- ASC order (should need filesort if index is DESC) ---");
            printQuery(stmt, "EXPLAIN SELECT * FROM notifications WHERE patient_id = 1 ORDER BY created_at ASC");
            System.out.println("--- DESC order (should NOT need filesort with DESC index) ---");
            printQuery(stmt, "EXPLAIN SELECT * FROM notifications WHERE patient_id = 1 ORDER BY created_at DESC");
        }
    }

    private void printQuery(Statement stmt, String sql) throws Exception {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            StringBuilder header = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) header.append(" | ");
                header.append(rsmd.getColumnLabel(i));
            }
            System.out.println(header);
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) row.append(" | ");
                    String val = rs.getString(i);
                    row.append(val == null ? "NULL" : val);
                }
                System.out.println(row);
            }
        }
    }
}
