package com.keystone.service;

import com.keystone.domain.entity.*;
import com.keystone.domain.enums.*;
import com.keystone.dto.request.*;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.WorkOrderResponse;
import com.keystone.exception.BusinessException;
import com.keystone.exception.ResourceNotFoundException;
import com.keystone.mapper.EntityMapper;
import com.keystone.repository.*;
import com.keystone.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;
    private final PartRepository partRepository;
    private final CustomerService customerService;
    private final WorkOrderStateMachine stateMachine;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> list(WorkOrderStatus status, String search,
                                                boolean openOnly, Pageable pageable,
                                                UserPrincipal actor) {
        Long customerId = null;
        Long assigneeId = null;

        if (Role.CUSTOMER.name().equals(actor.getRole())) {
            customerId = actor.getCustomerId();
        } else if (Role.TECHNICIAN.name().equals(actor.getRole())) {
            assigneeId = actor.getId();
        }

        Page<WorkOrder> page = workOrderRepository.search(
//                customerId, assigneeId, status, search, openOnly, pageable);
                customerId, assigneeId, status != null ? status.name() : null, search, openOnly, pageable);

        return PageResponse.<WorkOrderResponse>builder()
                .content(page.getContent().stream()
                        .map(wo -> mapper.toWorkOrderResponse(wo, false))
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(Long id, UserPrincipal actor) {
        WorkOrder wo = findWithAccess(id, actor);
        return mapper.toWorkOrderResponse(wo, true);
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest request, UserPrincipal actor) {
        Customer customer = customerService.findCustomer(request.getCustomerId());
        Site site = customerService.findSite(request.getSiteId());

        if (!site.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("Site does not belong to customer", HttpStatus.BAD_REQUEST);
        }

        User creator = userRepository.findById(actor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkOrder wo = WorkOrder.builder()
                .code(generateCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(WorkOrderStatus.NEW)
                .customer(customer)
                .site(site)
                .slaDueAt(slaService.calculateDueDate(request.getPriority()))
                .slaStatus(SlaStatus.ON_TRACK)
                .createdBy(creator)
                .build();

        WorkOrderStatusHistory history = WorkOrderStatusHistory.builder()
                .workOrder(wo)
                .fromStatus(null)
                .toStatus(WorkOrderStatus.NEW)
                .changedBy(creator)
                .note("Work order created")
                .build();
        wo.getStatusHistory().add(history);

        WorkOrder saved = workOrderRepository.save(wo);
        log.info("Work order created: {}", saved.getCode());
        return mapper.toWorkOrderResponse(saved, true);
    }

    @Transactional
    public WorkOrderResponse update(Long id, WorkOrderRequest request, UserPrincipal actor) {
        WorkOrder wo = findWithAccess(id, actor);

        if (!wo.isEditable()) {
            throw new BusinessException("Cannot edit closed or cancelled work order", HttpStatus.CONFLICT);
        }

        if (Role.CUSTOMER.name().equals(actor.getRole())) {
            throw new BusinessException("Customers cannot edit work orders", HttpStatus.FORBIDDEN);
        }

        Site site = customerService.findSite(request.getSiteId());
        if (!site.getCustomer().getId().equals(wo.getCustomer().getId())) {
            throw new BusinessException("Site does not belong to customer", HttpStatus.BAD_REQUEST);
        }

        wo.setTitle(request.getTitle());
        wo.setDescription(request.getDescription());
        wo.setPriority(request.getPriority());
        wo.setSite(site);
        wo.setSlaDueAt(slaService.calculateDueDate(request.getPriority()));

        return mapper.toWorkOrderResponse(workOrderRepository.save(wo), true);
    }

    @Transactional
    public WorkOrderResponse assign(Long id, AssignRequest request, UserPrincipal actor) {
        WorkOrder wo = findWithAccess(id, actor);

        if (!Role.DISPATCHER.name().equals(actor.getRole()) && !Role.MANAGER.name().equals(actor.getRole())) {
            throw new BusinessException("Only dispatchers can assign work orders", HttpStatus.FORBIDDEN);
        }

        if (!wo.isEditable()) {
            throw new BusinessException("Cannot assign closed or cancelled work order", HttpStatus.CONFLICT);
        }

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BusinessException("User is not a technician", HttpStatus.BAD_REQUEST);
        }

        User actorUser = userRepository.findById(actor.getId()).orElseThrow();

        WorkOrderStatus previousStatus = wo.getStatus();
        if (previousStatus == WorkOrderStatus.NEW) {
            stateMachine.validateTransition(previousStatus, WorkOrderStatus.ASSIGNED, actor,
                    technician.getId());
            addHistory(wo, previousStatus, WorkOrderStatus.ASSIGNED, actorUser,
                    "Assigned to " + technician.getFullName());
            wo.setStatus(WorkOrderStatus.ASSIGNED);
        } else {
            addHistory(wo, previousStatus, previousStatus, actorUser,
                    "Reassigned to " + technician.getFullName());
        }

        wo.setAssignee(technician);
        WorkOrder saved = workOrderRepository.save(wo);
        notificationService.notifyAssignment(saved, technician);
        log.info("Work order {} assigned to {}", saved.getCode(), technician.getFullName());
        return mapper.toWorkOrderResponse(saved, true);
    }

    @Transactional
    public WorkOrderResponse transitionStatus(Long id, StatusTransitionRequest request,
                                              UserPrincipal actor) {
        WorkOrder wo = findWithAccess(id, actor);
        User actorUser = userRepository.findById(actor.getId()).orElseThrow();

        WorkOrderStatus from = wo.getStatus();
        WorkOrderStatus to = request.getStatus();
        Long assigneeId = wo.getAssignee() != null ? wo.getAssignee().getId() : null;

        stateMachine.validateTransition(from, to, actor, assigneeId);

        if (to == WorkOrderStatus.ASSIGNED && wo.getAssignee() == null) {
            throw new BusinessException("Cannot transition to ASSIGNED without an assignee",
                    HttpStatus.BAD_REQUEST);
        }

        addHistory(wo, from, to, actorUser, request.getNote());
        wo.setStatus(to);

        if (to == WorkOrderStatus.CLOSED) {
            wo.setClosedAt(Instant.now());
        }

        WorkOrder saved = workOrderRepository.save(wo);
        log.info("Work order {} transitioned from {} to {}", saved.getCode(), from, to);
        return mapper.toWorkOrderResponse(saved, true);
    }

    @Transactional
    public WorkOrderResponse logParts(Long id, PartUsageRequest request, UserPrincipal actor) {
        WorkOrder wo = findWithAccess(id, actor);

        if (!Role.TECHNICIAN.name().equals(actor.getRole()) ||
                wo.getAssignee() == null || !wo.getAssignee().getId().equals(actor.getId())) {
            throw new BusinessException("Only the assigned technician can log parts", HttpStatus.FORBIDDEN);
        }

        if (wo.getStatus().isTerminal()) {
            throw new BusinessException("Cannot log parts on terminal work order", HttpStatus.CONFLICT);
        }

        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));

        if (part.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock for part: " + part.getSku(), HttpStatus.BAD_REQUEST);
        }

        User technician = userRepository.findById(actor.getId()).orElseThrow();

        PartUsage usage = PartUsage.builder()
                .workOrder(wo)
                .part(part)
                .quantity(request.getQuantity())
                .unitCost(part.getUnitCost())
                .loggedBy(technician)
                .build();
        wo.getPartUsages().add(usage);

        part.setStockQuantity(part.getStockQuantity() - request.getQuantity());
        partRepository.save(part);
        workOrderRepository.save(wo);

        return mapper.toWorkOrderResponse(wo, true);
    }

    @Transactional
    public WorkOrderResponse logTime(Long id, TimeLogRequest request, UserPrincipal actor) {
        WorkOrder wo = findWithAccess(id, actor);

        if (!Role.TECHNICIAN.name().equals(actor.getRole()) ||
                wo.getAssignee() == null || !wo.getAssignee().getId().equals(actor.getId())) {
            throw new BusinessException("Only the assigned technician can log time", HttpStatus.FORBIDDEN);
        }

        if (wo.getStatus().isTerminal()) {
            throw new BusinessException("Cannot log time on terminal work order", HttpStatus.CONFLICT);
        }

        User technician = userRepository.findById(actor.getId()).orElseThrow();

        TimeLog timeLog = TimeLog.builder()
                .workOrder(wo)
                .technician(technician)
                .minutes(request.getMinutes())
                .note(request.getNote())
                .build();
        wo.getTimeLogs().add(timeLog);
        workOrderRepository.save(wo);

        return mapper.toWorkOrderResponse(wo, true);
    }

    @Transactional
    public WorkOrderResponse createCustomerRequest(CustomerPortalRequest request) {
        Site site = customerService.findSite(request.getSiteId());
        Customer customer = site.getCustomer();

        if (!customer.getContactEmail().equalsIgnoreCase(request.getContactEmail())) {
            throw new BusinessException("Contact email does not match customer record",
                    HttpStatus.FORBIDDEN);
        }

        return createCustomerWorkOrder(request.getTitle(), request.getDescription(),
                request.getPriority(), site, customer, "Customer request submitted");
    }

    @Transactional
    public WorkOrderResponse createAuthenticatedCustomerRequest(CustomerAuthenticatedRequest request,
                                                                UserPrincipal actor) {
        if (actor.getCustomerId() == null) {
            throw new BusinessException("Customer account not linked to organisation", HttpStatus.FORBIDDEN);
        }

        Site site = customerService.findSite(request.getSiteId());
        if (!site.getCustomer().getId().equals(actor.getCustomerId())) {
            throw new BusinessException("Site does not belong to your organisation", HttpStatus.FORBIDDEN);
        }

        Customer customer = site.getCustomer();
        return createCustomerWorkOrder(request.getTitle(), request.getDescription(),
                request.getPriority(), site, customer, "Customer request submitted");
    }

    private WorkOrderResponse createCustomerWorkOrder(String title, String description,
                                                        Priority priority, Site site,
                                                        Customer customer, String historyNote) {
        User creator = userRepository.findByEmail("dispatcher@meridian.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No system user found")));

        WorkOrder wo = WorkOrder.builder()
                .code(generateCode())
                .title(title)
                .description(description)
                .priority(priority)
                .status(WorkOrderStatus.NEW)
                .customer(customer)
                .site(site)
                .slaDueAt(slaService.calculateDueDate(priority))
                .slaStatus(SlaStatus.ON_TRACK)
                .createdBy(creator)
                .build();

        WorkOrderStatusHistory history = WorkOrderStatusHistory.builder()
                .workOrder(wo)
                .fromStatus(null)
                .toStatus(WorkOrderStatus.NEW)
                .changedBy(creator)
                .note(historyNote)
                .build();
        wo.getStatusHistory().add(history);

        WorkOrder saved = workOrderRepository.save(wo);
        log.info("Customer request created: {}", saved.getCode());
        return mapper.toCustomerWorkOrderResponse(saved);
    }

    private WorkOrder findWithAccess(Long id, UserPrincipal actor) {
        WorkOrder wo = workOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));

        if (Role.CUSTOMER.name().equals(actor.getRole()) &&
                !wo.getCustomer().getId().equals(actor.getCustomerId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (Role.TECHNICIAN.name().equals(actor.getRole()) &&
                (wo.getAssignee() == null || !wo.getAssignee().getId().equals(actor.getId()))) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        return wo;
    }

    private void addHistory(WorkOrder wo, WorkOrderStatus from, WorkOrderStatus to,
                            User changedBy, String note) {
        WorkOrderStatusHistory history = WorkOrderStatusHistory.builder()
                .workOrder(wo)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .note(note)
                .build();
        wo.getStatusHistory().add(history);
    }

    private synchronized String generateCode() {
        long count = workOrderRepository.count() + 1;
        return String.format("WO-%d-%04d", Year.now().getValue(), count);
    }
}
