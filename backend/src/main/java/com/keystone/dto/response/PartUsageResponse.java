package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PartUsageResponse {
    private Long id;
    private Long partId;
    private String partName;
    private String partSku;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String loggedByName;
    private Instant loggedAt;
}
