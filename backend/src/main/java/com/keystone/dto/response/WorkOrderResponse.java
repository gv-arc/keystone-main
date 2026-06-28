package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class WorkOrderResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private String priority;
    private String status;
    private Long customerId;
    private String customerName;
    private Long siteId;
    private String siteName;
    private Long assigneeId;
    private String assigneeName;
    private Instant slaDueAt;
    private String slaStatus;
    private BigDecimal totalPartsCost;
    private int totalMinutesLogged;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private List<StatusHistoryResponse> statusHistory;
    private List<PartUsageResponse> partUsages;
    private List<TimeLogResponse> timeLogs;
}
