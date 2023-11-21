package com.example.security.reservation.dto;

import com.example.security.reservation.entity.UserClub;
import com.example.security.user.User;

import java.util.List;

public record ClubDto(Integer id,
                      String name,
                      User user,
                      List<UserClubDto> followers) {
}
