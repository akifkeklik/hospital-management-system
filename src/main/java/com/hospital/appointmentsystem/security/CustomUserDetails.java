package com.hospital.appointmentsystem.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long referenceId;
    private final String role;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long referenceId, String role) {
        super(username, password, authorities);
        this.referenceId = referenceId;
        this.role = role;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getRole() {
        return role;
    }
}
