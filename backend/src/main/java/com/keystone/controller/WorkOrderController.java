package com.keystone.controller;

import com.keystone.domain.enums.WorkOrderStatus;
import com.keystone.dto.request.*;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.WorkOrderResponse;
import com.keystone.security.SecurityUtils;
import com.keystone.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    @Operation(summary = "List work orders (role-scoped, paginated)")
    public PageResponse<WorkOrderResponse> list(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean openOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "created_at,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return workOrderService.list(status, search, openOnly,
                PageRequest.of(page, size, Sort.by(direction, sortParts[0])),
                SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get work order with history")
    public WorkOrderResponse getById(@PathVariable Long id) {
        return workOrderService.getById(id, SecurityUtils.currentUser());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a work order")
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request) {
        return workOrderService.create(request, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Operation(summary = "Update work order while open")
    public WorkOrderResponse update(@PathVariable Long id,
                                    @Valid @RequestBody WorkOrderRequest request) {
        return workOrderService.update(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Operation(summary = "Assign work order to technician")
    public WorkOrderResponse assign(@PathVariable Long id,
                                    @Valid @RequestBody AssignRequest request) {
        return workOrderService.assign(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Transition work order status")
    public WorkOrderResponse transitionStatus(@PathVariable Long id,
                                              @Valid @RequestBody StatusTransitionRequest request) {
        return workOrderService.transitionStatus(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/parts")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Log parts used on work order")
    public WorkOrderResponse logParts(@PathVariable Long id,
                                      @Valid @RequestBody PartUsageRequest request) {
        return workOrderService.logParts(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/time")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Log time on work order")
    public WorkOrderResponse logTime(@PathVariable Long id,
                                     @Valid @RequestBody TimeLogRequest request) {
        return workOrderService.logTime(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/customer-request")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit service request (authenticated customer)")
    public WorkOrderResponse createCustomerRequest(@Valid @RequestBody CustomerAuthenticatedRequest request) {
        return workOrderService.createAuthenticatedCustomerRequest(request, SecurityUtils.currentUser());
    }
}
