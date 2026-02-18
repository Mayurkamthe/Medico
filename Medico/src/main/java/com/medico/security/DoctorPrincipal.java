package com.medico.security;

import com.medico.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class DoctorPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private String fullName;

    public static DoctorPrincipal create(Doctor doctor) {
        return new DoctorPrincipal(
                doctor.getId(),
                doctor.getEmail(),
                doctor.getPassword(),
                doctor.getFullName());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
