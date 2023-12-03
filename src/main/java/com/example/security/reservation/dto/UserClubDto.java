package com.example.security.reservation.dto;

import com.example.security.reservation.entity.Club;

public record UserClubDto(Integer id,
                          Club club,
                          UserDto user) {
}
