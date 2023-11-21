package com.example.security.reservation.dto;

import com.example.security.reservation.entity.Stadium;

public record StadiumCreationRequest(Stadium stadium, int clubId) {
}
