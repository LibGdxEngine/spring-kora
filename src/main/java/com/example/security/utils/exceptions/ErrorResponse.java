package com.example.security.utils.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public record ErrorResponse(LocalDateTime timestamp,
                            int status,
                            String error) {

}
