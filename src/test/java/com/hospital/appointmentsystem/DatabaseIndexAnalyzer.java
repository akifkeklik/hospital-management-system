package com.hospital.appointmentsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

@SpringBootTest
public class DatabaseIndexAnalyzer {

    @Autowired
    private DataSource dataSource;

    @Test
    public void runExplainQueries() throws Exception {
        String[] queries = {
            "SELECT VERSION()",
            // Patient Queries
            "EXPLAIN SELECT * FROM patients WHERE tc_identity_number = '12345678901'",
            
            // User Queries
            "EXPLAIN SELECT * FROM users WHERE username = 'testuser'",
            "EXPLAIN SELECT * FROM users WHERE email = 'test@test.com'",

            // Appointment Queries
            "EXPLAIN SELECT * FROM appointments WHERE patient_id = 1",
            "EXPLAIN SELECT * FROM appointments WHERE doctor_id = 1",
            "EXPLAIN SELECT * FROM appointments WHERE status = 'SCHEDULED'",
            "EXPLAIN SELECT * FROM appointments WHERE doctor_id = 1 AND status = 'SCHEDULED' AND appointment_date BETWEEN '2026-08-01' AND '2026-08-30'",

            // Doctor Queries
            "EXPLAIN SELECT * FROM doctors WHERE department_id = 1",
            
            // Doctor Leave Queries
            "EXPLAIN SELECT * FROM doctor_leaves WHERE doctor_id = 1",
            "EXPLAIN SELECT * FROM doctor_leaves WHERE doctor_id = 1 AND '2026-08-25' BETWEEN start_date AND end_date",

            // Notification Queries
            "EXPLAIN SELECT * FROM notifications WHERE patient_id = 1 ORDER BY created_at DESC",
            "EXPLAIN SELECT * FROM notifications WHERE patient_id = 1 AND is_read = false",
            "EXPLAIN SELECT * FROM notifications WHERE doctor_id = 1 ORDER BY created_at DESC",
            "EXPLAIN SELECT * FROM notifications WHERE doctor_id = 1 AND is_read = false"
        };

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String query : queries) {
                System.out.println("=========================================");
                System.out.println("QUERY: " + query.replace("EXPLAIN ", ""));
                System.out.println("-----------------------------------------");
                try (ResultSet rs = stmt.executeQuery(query)) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    
                    // Print header
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rsmd.getColumnName(i) + "\t| ");
                    }
                    System.out.println();
                    
                    // Print rows
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.print(rs.getString(i) + "\t| ");
                        }
                        System.out.println();
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        }
    }
}
