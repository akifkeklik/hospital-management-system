package com.hospital.appointmentsystem.doctor;

import com.hospital.appointmentsystem.department.impl.Department;
import com.hospital.appointmentsystem.department.impl.DepartmentRepository;
import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.user.api.UserService;
import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DoctorServiceIntegrationTest {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateDoctorAndUserAccountWithCorrectRolesAndFlags() {
        // Arrange
        // Create a real department first
        Department department = new Department();
        department.setName("Neurology");
        department.setDescription("Brain stuff");
        department = departmentRepository.save(department);

        DoctorDto doctorDto = new DoctorDto();
        doctorDto.setFirstName("Veli");
        doctorDto.setLastName("Gök");
        doctorDto.setTcIdentityNumber("98765432101");
        doctorDto.setEmail("veli.gok@hospital.com");
        doctorDto.setPhoneNumber("5551234567");
        doctorDto.setSpecialization("Surgeon");
        doctorDto.setDepartmentId(department.getId());

        // Act
        DoctorDto createdDoctor = doctorService.createDoctor(doctorDto);

        // Assert - Doctor creation
        assertNotNull(createdDoctor.getId());
        assertEquals("Veli", createdDoctor.getFirstName());

        // Assert - User account creation business rules
        User createdUser = userRepository.findByUsername("98765432101").orElse(null);
        
        assertNotNull(createdUser, "User account should be automatically created for the doctor");
        assertEquals("98765432101", createdUser.getUsername());
        assertEquals("veli.gok@hospital.com", createdUser.getEmail());
        assertEquals("ROLE_DOCTOR", createdUser.getRole(), "User should have ROLE_DOCTOR role");
        assertTrue(createdUser.getNeedsPasswordChange(), "NeedsPasswordChange flag should be true for newly created doctor accounts");
        assertEquals(createdDoctor.getId(), createdUser.getReferenceId(), "User account should be linked to the newly created doctor");
        
        // Assert - Default password logic
        // We know the password is encoded using PasswordEncoder in registerUser, 
        // so we cannot directly compare with "987654". But we know registerUser 
        // sets a non-null password.
        assertNotNull(createdUser.getPassword());
    }
}
