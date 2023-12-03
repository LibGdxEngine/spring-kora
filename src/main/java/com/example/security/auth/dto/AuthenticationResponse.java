package com.example.security.auth.dto;

import com.example.security.reservation.entity.Club;
import com.example.security.reservation.entity.UserClub;

import java.util.List;

public record AuthenticationResponse(String token, String email, String role, List<Integer> followedClubsIds) {
}
