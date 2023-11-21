package com.example.security.auth.dto;

public record PreRegistrationResponse(String token, String message, Integer statusCode) {
}
