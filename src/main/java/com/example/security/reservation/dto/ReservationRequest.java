package com.example.security.reservation.dto;

import com.example.security.user.User;

import java.time.LocalDateTime;

public record ReservationRequest(int stadiumId, LocalDateTime reservationTime, String playerName) {
}
