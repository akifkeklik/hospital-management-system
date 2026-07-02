package com.hospital.appointmentsystem.polyclinic.api;

public class PolyclinicDto {
    private Long id;
    private String name;
    private String roomNumber;
    private Long departmentId;

    public PolyclinicDto() {}

    public PolyclinicDto(Long id, String name, String roomNumber, Long departmentId) {
        this.id = id;
        this.name = name;
        this.roomNumber = roomNumber;
        this.departmentId = departmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
