package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TimeLogResponse {
    private Long id;
    private int minutes;
    private String note;
    private String technicianName;
    private Instant loggedAt;
}
