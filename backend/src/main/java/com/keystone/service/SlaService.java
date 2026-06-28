package com.keystone.service;

import com.keystone.domain.enums.Priority;
import com.keystone.domain.enums.SlaStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SlaService {

    private final int criticalHours;
    private final int highHours;
    private final int mediumHours;
    private final int lowHours;

    public SlaService(
            @Value("${keystone.sla.critical-hours}") int criticalHours,
            @Value("${keystone.sla.high-hours}") int highHours,
            @Value("${keystone.sla.medium-hours}") int mediumHours,
            @Value("${keystone.sla.low-hours}") int lowHours) {
        this.criticalHours = criticalHours;
        this.highHours = highHours;
        this.mediumHours = mediumHours;
        this.lowHours = lowHours;
    }

    public Instant calculateDueDate(Priority priority) {
        int hours = switch (priority) {
            case CRITICAL -> criticalHours;
            case HIGH -> highHours;
            case MEDIUM -> mediumHours;
            case LOW -> lowHours;
        };
        return Instant.now().plus(hours, ChronoUnit.HOURS);
    }

    public SlaStatus evaluateSlaStatus(Instant slaDueAt, SlaStatus current) {
        if (slaDueAt == null) {
            return SlaStatus.ON_TRACK;
        }
        Instant now = Instant.now();
        if (now.isAfter(slaDueAt)) {
            return SlaStatus.BREACHED;
        }
        long totalWindow = ChronoUnit.MINUTES.between(now.minus(
                ChronoUnit.HOURS.between(now, slaDueAt), ChronoUnit.MINUTES), slaDueAt);
        long remaining = ChronoUnit.MINUTES.between(now, slaDueAt);
        if (totalWindow > 0 && remaining <= totalWindow * 0.25) {
            return SlaStatus.AT_RISK;
        }
        return current == SlaStatus.BREACHED ? SlaStatus.BREACHED : SlaStatus.ON_TRACK;
    }
}
