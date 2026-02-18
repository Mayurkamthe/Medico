package com.medico.service;

import com.medico.dto.*;
import com.medico.entity.Doctor;
import com.medico.repository.DoctorRepository;
import com.medico.security.DoctorPrincipal;
import com.medico.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Doctor doctor = Doctor.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .phoneNumber(request.getPhoneNumber())
                .build();

        doctor = doctorRepository.save(doctor);

        String token = tokenProvider.generateToken(doctor.getId(), doctor.getEmail());

        return new AuthResponse(token, DoctorResponse.fromEntity(doctor));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        DoctorPrincipal principal = (DoctorPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);

        Doctor doctor = doctorRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return new AuthResponse(token, DoctorResponse.fromEntity(doctor));
    }

    public DoctorResponse getCurrentDoctor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        DoctorPrincipal principal = (DoctorPrincipal) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return DoctorResponse.fromEntity(doctor);
    }

    @Transactional
    public void updatePushToken(Long doctorId, String pushToken) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setExpoPushToken(pushToken);
        doctorRepository.save(doctor);
    }
}
