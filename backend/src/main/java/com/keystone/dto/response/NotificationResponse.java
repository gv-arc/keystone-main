package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String message;
    private Long workOrderId;
    private String workOrderCode;
    private boolean read;
    private Instant createdAt;
}
