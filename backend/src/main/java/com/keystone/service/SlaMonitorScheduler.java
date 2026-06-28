package com.keystone.service;

import com.keystone.domain.entity.WorkOrder;
import com.keystone.domain.enums.SlaStatus;
import com.keystone.domain.enums.WorkOrderStatus;
import com.keystone.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaMonitorScheduler {

    private final WorkOrderRepository workOrderRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkSlaBreaches() {
        List<WorkOrder> openOrders = workOrderRepository.findByStatusNotIn(
                List.of(WorkOrderStatus.CLOSED, WorkOrderStatus.CANCELLED));

        Instant now = Instant.now();
        for (WorkOrder wo : openOrders) {
            if (wo.getSlaDueAt() == null) {
                continue;
            }

            SlaStatus previous = wo.getSlaStatus();
            SlaStatus updated = evaluate(wo, now);

            if (updated != previous) {
                wo.setSlaStatus(updated);
                workOrderRepository.save(wo);
                if (updated == SlaStatus.AT_RISK) {
                    notificationService.notifySlaStatus(wo,
                            com.keystone.domain.enums.NotificationType.SLA_AT_RISK);
                    log.warn("SLA at risk for work order {}", wo.getCode());
                } else if (updated == SlaStatus.BREACHED) {
                    notificationService.notifySlaStatus(wo,
                            com.keystone.domain.enums.NotificationType.SLA_BREACHED);
                    log.warn("SLA breached for work order {}", wo.getCode());
                }
            }
        }
    }

    private SlaStatus evaluate(WorkOrder wo, Instant now) {
        if (now.isAfter(wo.getSlaDueAt())) {
            return SlaStatus.BREACHED;
        }
        long remainingMinutes = ChronoUnit.MINUTES.between(now, wo.getSlaDueAt());
        long totalMinutes = ChronoUnit.MINUTES.between(wo.getCreatedAt(), wo.getSlaDueAt());
        if (totalMinutes > 0 && remainingMinutes <= totalMinutes * 0.25) {
            return SlaStatus.AT_RISK;
        }
        return SlaStatus.ON_TRACK;
    }
}
