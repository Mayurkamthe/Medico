package com.medico.controller;

import com.medico.dto.*;
import com.medico.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DoctorResponse>> getCurrentDoctor() {
        try {
            DoctorResponse response = authService.getCurrentDoctor();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/push-token")
    public ResponseEntity<ApiResponse<String>> updatePushToken(@RequestBody PushTokenRequest request) {
        try {
            DoctorResponse doctor = authService.getCurrentDoctor();
            authService.updatePushToken(doctor.getId(), request.getPushToken());
            return ResponseEntity.ok(ApiResponse.success("Push token updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
