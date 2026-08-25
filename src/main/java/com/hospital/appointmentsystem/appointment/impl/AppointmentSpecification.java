package com.hospital.appointmentsystem.appointment.impl;

import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

public class AppointmentSpecification {

    public static Specification<Appointment> filterBy(Long patientId, Long doctorId, List<AppointmentStatus> statuses, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));
            }

            if (doctorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("doctor").get("id"), doctorId));
            }

            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("appointmentDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("appointmentDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
