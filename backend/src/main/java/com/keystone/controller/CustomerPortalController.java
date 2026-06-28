package com.keystone.controller;

import com.keystone.dto.request.CustomerPortalRequest;
import com.keystone.dto.response.WorkOrderResponse;
import com.keystone.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Customer Portal")
public class CustomerPortalController {

    private final WorkOrderService workOrderService;

    @PostMapping("/api/customer-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a customer service request (anonymous)")
    public WorkOrderResponse submitRequest(@Valid @RequestBody CustomerPortalRequest request) {
        return workOrderService.createCustomerRequest(request);
    }
}
