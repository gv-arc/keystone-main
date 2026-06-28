package com.keystone.service;

import com.keystone.domain.entity.WorkOrder;
import com.keystone.domain.enums.SlaStatus;
import com.keystone.domain.enums.WorkOrderStatus;
import com.keystone.dto.response.ReportSummaryResponse;
import com.keystone.dto.response.SiteLoadResponse;
import com.keystone.dto.response.TechnicianLoadResponse;
import com.keystone.repository.WorkOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final WorkOrderRepository workOrderRepository;

    @Transactional
    public ReportSummaryResponse getSummary() {

        Map<String, Long> statusCounts = new HashMap<>();

        for (WorkOrderStatus status : WorkOrderStatus.values()) {
            statusCounts.put(status.name(), workOrderRepository.countByStatus(status));
        }

        long overdue = workOrderRepository.countOverdue(Instant.now());

        List<WorkOrder> openOrders = workOrderRepository.findByStatusNotIn(
                List.of(
                        WorkOrderStatus.CLOSED,
                        WorkOrderStatus.CANCELLED
                )
        );

        long onTrack = openOrders.stream()
                .filter(order -> order.getSlaStatus() == SlaStatus.ON_TRACK)
                .count();

        long atRisk = openOrders.stream()
                .filter(order -> order.getSlaStatus() == SlaStatus.AT_RISK)
                .count();

        long breached = openOrders.stream()
                .filter(order -> order.getSlaStatus() == SlaStatus.BREACHED)
                .count();

        List<TechnicianLoadResponse> byTechnician =
                workOrderRepository.countOpenByTechnician()
                        .stream()
                        .map(row -> TechnicianLoadResponse.builder()
                                .technicianId(((Number) row[0]).longValue())
                                .technicianName((String) row[1])
                                .openCount(((Number) row[2]).longValue())
                                .build())
                        .toList();

        List<SiteLoadResponse> bySite =
                workOrderRepository.countOpenBySite()
                        .stream()
                        .map(row -> SiteLoadResponse.builder()
                                .siteId(((Number) row[0]).longValue())
                                .siteName((String) row[1])
                                .openCount(((Number) row[2]).longValue())
                                .build())
                        .toList();

        return ReportSummaryResponse.builder()
                .statusCounts(statusCounts)
                .overdueCount(overdue)
                .slaOnTrack(onTrack)
                .slaAtRisk(atRisk)
                .slaBreached(breached)
                .byTechnician(byTechnician)
                .bySite(bySite)
                .build();
    }
}