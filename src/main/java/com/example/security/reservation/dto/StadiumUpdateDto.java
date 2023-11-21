package com.example.security.reservation.dto;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;

import java.util.List;

public record StadiumUpdateDto(String name,
                               String size,
                               String features,
                               String cost,
                               List<String> images,
                               String description) {
}
