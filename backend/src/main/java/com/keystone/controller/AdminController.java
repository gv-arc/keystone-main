package com.keystone.controller;

import com.keystone.dto.request.PartRequest;
import com.keystone.dto.request.UserRequest;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.PartResponse;
import com.keystone.dto.response.UserResponse;
import com.keystone.service.PartService;
import com.keystone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Administration")
public class AdminController {

    private final UserService userService;
    private final PartService partService;

    @GetMapping("/api/users")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "List users")
    public PageResponse<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.list(PageRequest.of(page, size, Sort.by("fullName")));
    }

    @PostMapping("/api/users")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user")
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/api/users/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @GetMapping("/api/technicians")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Operation(summary = "List active technicians")
    public List<UserResponse> listTechnicians() {
        return userService.listTechnicians();
    }

    @GetMapping("/api/parts")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "List parts inventory")
    public PageResponse<PartResponse> listParts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return partService.list(search, PageRequest.of(page, size, Sort.by("name")));
    }

    @PostMapping("/api/parts")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create part")
    public PartResponse createPart(@Valid @RequestBody PartRequest request) {
        return partService.create(request);
    }

    @PutMapping("/api/parts/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update part")
    public PartResponse updatePart(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        return partService.update(id, request);
    }
}
