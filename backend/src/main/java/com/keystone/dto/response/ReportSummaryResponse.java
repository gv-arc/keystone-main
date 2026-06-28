package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ReportSummaryResponse {
    private Map<String, Long> statusCounts;
    private long overdueCount;
    private long slaOnTrack;
    private long slaAtRisk;
    private long slaBreached;
    private List<TechnicianLoadResponse> byTechnician;
    private List<SiteLoadResponse> bySite;
}