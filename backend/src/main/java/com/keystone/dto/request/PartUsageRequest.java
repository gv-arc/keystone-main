package com.keystone.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartUsageRequest {

    @NotNull
    private Long partId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
