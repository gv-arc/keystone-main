package com.keystone.controller;

import com.keystone.dto.response.NotificationResponse;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.ReportSummaryResponse;
import com.keystone.mapper.EntityMapper;
import com.keystone.repository.NotificationRepository;
import com.keystone.security.SecurityUtils;
import com.keystone.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports & Notifications")
public class ReportController {

    private final ReportService reportService;
    private final NotificationRepository notificationRepository;
    private final EntityMapper mapper;

    @GetMapping("/api/reports/summary")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Dashboard metrics summary")
    public ReportSummaryResponse getSummary() {
        return reportService.getSummary();
    }

    @GetMapping("/api/notifications")
    @Operation(summary = "List notifications for current user")
    public PageResponse<NotificationResponse> listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var userId = SecurityUtils.currentUser().getId();
        var result = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size));
        return PageResponse.<NotificationResponse>builder()
                .content(result.getContent().stream().map(mapper::toNotificationResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @PatchMapping("/api/notifications/{id}/read")
    @Operation(summary = "Mark notification as read")
    public NotificationResponse markRead(@PathVariable Long id) {
        var notification = notificationRepository.findById(id)
                .orElseThrow(() -> new com.keystone.exception.ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(SecurityUtils.currentUser().getId())) {
            throw new com.keystone.exception.BusinessException("Access denied",
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        notification.setRead(true);
        return mapper.toNotificationResponse(notificationRepository.save(notification));
    }
}
