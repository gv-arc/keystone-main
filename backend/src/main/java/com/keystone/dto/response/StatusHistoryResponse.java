package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class StatusHistoryResponse {
    private Long id;
    private String fromStatus;
    private String toStatus;
    private String changedByName;
    private String note;
    private Instant changedAt;
}
