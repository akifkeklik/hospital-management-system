package com.hospital.appointmentsystem.user.api;

public interface UserService {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void registerUser(String username, String email, String password, String role, Long referenceId);
    boolean resetPassword(String username, String email, String newPassword);
    void setNeedsPasswordChange(String username, boolean flag);
    boolean changePassword(String username, String newPassword);
    boolean changePasswordWithOld(String username, String oldPassword, String newPassword);
}
