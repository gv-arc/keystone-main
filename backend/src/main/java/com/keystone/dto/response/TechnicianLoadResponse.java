package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public  class TechnicianLoadResponse {
    private Long technicianId;
    private String technicianName;
    private long openCount;
}
