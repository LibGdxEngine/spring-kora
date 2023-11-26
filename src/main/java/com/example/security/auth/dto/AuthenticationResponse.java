package com.example.security.auth.dto;

public record AuthenticationResponse(String token, String email, String role) {
}
