package com.keystone.dto.request;

import com.keystone.domain.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusTransitionRequest {

    @NotNull
    private WorkOrderStatus status;

    private String note;
}
