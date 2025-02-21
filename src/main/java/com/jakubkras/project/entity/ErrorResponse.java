package com.jakubkras.project.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class ErrorResponse {

    private String message;

    private HttpStatus status;

    private LocalDateTime timestamp;

    private String path;
}
