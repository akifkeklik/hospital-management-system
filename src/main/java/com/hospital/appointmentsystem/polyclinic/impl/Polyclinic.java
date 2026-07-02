package com.hospital.appointmentsystem.polyclinic.impl;

import jakarta.persistence.*;

@Entity
@Table(name = "polyclinics")
public class Polyclinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    public Polyclinic() {
    }

    public Polyclinic(String name, String roomNumber, Long departmentId) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.departmentId = departmentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
