package com.medico.security;

import com.medico.entity.Doctor;
import com.medico.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with email: " + email));

        return DoctorPrincipal.create(doctor);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with id: " + id));

        return DoctorPrincipal.create(doctor);
    }
}
