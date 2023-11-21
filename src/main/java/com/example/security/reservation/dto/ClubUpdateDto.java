package com.example.security.reservation.dto;

public record ClubUpdateDto(String name,
                            String address,
                            Double latitude,
                            Double longitude) {
}
