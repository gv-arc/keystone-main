package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PartResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal unitCost;
    private int stockQuantity;
}
