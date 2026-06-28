package com.keystone.controller;

import com.keystone.dto.request.CustomerRequest;
import com.keystone.dto.request.SiteRequest;
import com.keystone.dto.response.CustomerResponse;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.SiteResponse;
import com.keystone.security.SecurityUtils;
import com.keystone.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers & Sites")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "List customers (paginated, searchable)")
    public PageResponse<CustomerResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return customerService.list(search, PageRequest.of(page, size, Sort.by("name")), SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerService.getById(id, SecurityUtils.currentUser());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create customer")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Operation(summary = "Update customer")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @GetMapping("/{customerId}/sites")
    @Operation(summary = "List sites for customer")
    public PageResponse<SiteResponse> listSites(
            @PathVariable Long customerId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return customerService.listSites(customerId, search, PageRequest.of(page, size, Sort.by("name")),
                SecurityUtils.currentUser());
    }

    @PostMapping("/{customerId}/sites")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create site for customer")
    public SiteResponse createSite(@PathVariable Long customerId, @Valid @RequestBody SiteRequest request) {
        return customerService.createSite(customerId, request);
    }

    @PutMapping("/{customerId}/sites/{siteId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Operation(summary = "Update site")
    public SiteResponse updateSite(@PathVariable Long customerId, @PathVariable Long siteId,
                                   @Valid @RequestBody SiteRequest request) {
        return customerService.updateSite(customerId, siteId, request);
    }
}
