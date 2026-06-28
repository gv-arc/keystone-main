package com.keystone.mapper;

import com.keystone.domain.entity.*;
import com.keystone.dto.response.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EntityMapper {

    public CustomerResponse toCustomerResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .contactEmail(c.getContactEmail())
                .contactPhone(c.getContactPhone())
                .address(c.getAddress())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public SiteResponse toSiteResponse(Site s) {
        return SiteResponse.builder()
                .id(s.getId())
                .customerId(s.getCustomer().getId())
                .customerName(s.getCustomer().getName())
                .name(s.getName())
                .address(s.getAddress())
                .city(s.getCity())
                .postcode(s.getPostcode())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole().name())
                .customerId(u.getCustomer() != null ? u.getCustomer().getId() : null)
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .build();
    }

    public PartResponse toPartResponse(Part p) {
        return PartResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .unitCost(p.getUnitCost())
                .stockQuantity(p.getStockQuantity())
                .build();
    }

    public WorkOrderResponse toWorkOrderResponse(WorkOrder wo, boolean includeDetails) {
        WorkOrderResponse.WorkOrderResponseBuilder builder = WorkOrderResponse.builder()
                .id(wo.getId())
                .code(wo.getCode())
                .title(wo.getTitle())
                .description(wo.getDescription())
                .priority(wo.getPriority().name())
                .status(wo.getStatus().name())
                .customerId(wo.getCustomer().getId())
                .customerName(wo.getCustomer().getName())
                .siteId(wo.getSite().getId())
                .siteName(wo.getSite().getName())
                .assigneeId(wo.getAssignee() != null ? wo.getAssignee().getId() : null)
                .assigneeName(wo.getAssignee() != null ? wo.getAssignee().getFullName() : null)
                .slaDueAt(wo.getSlaDueAt())
                .slaStatus(wo.getSlaStatus().name())
                .totalPartsCost(wo.getTotalPartsCost())
                .totalMinutesLogged(wo.getTotalMinutesLogged())
                .createdAt(wo.getCreatedAt())
                .updatedAt(wo.getUpdatedAt())
                .closedAt(wo.getClosedAt());

        if (includeDetails) {
            builder.statusHistory(wo.getStatusHistory().stream().map(this::toHistoryResponse).toList())
                    .partUsages(wo.getPartUsages().stream().map(this::toPartUsageResponse).toList())
                    .timeLogs(wo.getTimeLogs().stream().map(this::toTimeLogResponse).toList());
        }

        return builder.build();
    }

    public WorkOrderResponse toCustomerWorkOrderResponse(WorkOrder wo) {
        return WorkOrderResponse.builder()
                .id(wo.getId())
                .code(wo.getCode())
                .title(wo.getTitle())
                .description(wo.getDescription())
                .priority(wo.getPriority().name())
                .status(wo.getStatus().name())
                .siteId(wo.getSite().getId())
                .siteName(wo.getSite().getName())
                .slaDueAt(wo.getSlaDueAt())
                .slaStatus(wo.getSlaStatus().name())
                .createdAt(wo.getCreatedAt())
                .updatedAt(wo.getUpdatedAt())
                .statusHistory(wo.getStatusHistory().stream()
                        .map(h -> StatusHistoryResponse.builder()
                                .id(h.getId())
                                .fromStatus(h.getFromStatus() != null ? h.getFromStatus().name() : null)
                                .toStatus(h.getToStatus().name())
                                .note(h.getNote())
                                .changedAt(h.getChangedAt())
                                .build())
                        .toList())
                .build();
    }

    public StatusHistoryResponse toHistoryResponse(WorkOrderStatusHistory h) {
        return StatusHistoryResponse.builder()
                .id(h.getId())
                .fromStatus(h.getFromStatus() != null ? h.getFromStatus().name() : null)
                .toStatus(h.getToStatus().name())
                .changedByName(h.getChangedBy().getFullName())
                .note(h.getNote())
                .changedAt(h.getChangedAt())
                .build();
    }

    public PartUsageResponse toPartUsageResponse(PartUsage u) {
        BigDecimal total = u.getUnitCost().multiply(BigDecimal.valueOf(u.getQuantity()));
        return PartUsageResponse.builder()
                .id(u.getId())
                .partId(u.getPart().getId())
                .partName(u.getPart().getName())
                .partSku(u.getPart().getSku())
                .quantity(u.getQuantity())
                .unitCost(u.getUnitCost())
                .totalCost(total)
                .loggedByName(u.getLoggedBy().getFullName())
                .loggedAt(u.getLoggedAt())
                .build();
    }

    public TimeLogResponse toTimeLogResponse(TimeLog t) {
        return TimeLogResponse.builder()
                .id(t.getId())
                .minutes(t.getMinutes())
                .note(t.getNote())
                .technicianName(t.getTechnician().getFullName())
                .loggedAt(t.getLoggedAt())
                .build();
    }

    public NotificationResponse toNotificationResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .workOrderId(n.getWorkOrder() != null ? n.getWorkOrder().getId() : null)
                .workOrderCode(n.getWorkOrder() != null ? n.getWorkOrder().getCode() : null)
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
