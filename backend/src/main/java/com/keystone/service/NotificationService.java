package com.keystone.service;

import com.keystone.domain.entity.Customer;
import com.keystone.domain.entity.Notification;
import com.keystone.domain.entity.User;
import com.keystone.domain.entity.WorkOrder;
import com.keystone.domain.enums.NotificationType;
import com.keystone.repository.NotificationRepository;
import com.keystone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifyAssignment(WorkOrder workOrder, User technician) {
        Notification notification = Notification.builder()
                .user(technician)
                .workOrder(workOrder)
                .type(NotificationType.ASSIGNMENT)
                .message("Work order " + workOrder.getCode() + " assigned to you: " + workOrder.getTitle())
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifySlaStatus(WorkOrder workOrder, NotificationType type) {
        userRepository.findByRoleAndActiveTrue(com.keystone.domain.enums.Role.MANAGER)
                .forEach(manager -> {
                    String msg = type == NotificationType.SLA_BREACHED
                            ? "SLA breached for " + workOrder.getCode()
                            : "SLA at risk for " + workOrder.getCode();
                    notificationRepository.save(Notification.builder()
                            .user(manager)
                            .workOrder(workOrder)
                            .type(type)
                            .message(msg)
                            .build());
                });
    }
}
