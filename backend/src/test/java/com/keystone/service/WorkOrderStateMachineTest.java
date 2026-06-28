package com.keystone.service;

import com.keystone.domain.enums.WorkOrderStatus;
import com.keystone.exception.BusinessException;
import com.keystone.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderStateMachineTest {

    private WorkOrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new WorkOrderStateMachine();
    }

    @Test
    void allowsValidTransitionFromNewToAssigned() {
        UserPrincipal dispatcher = principal("DISPATCHER", 1L);
        assertDoesNotThrow(() ->
                stateMachine.validateTransition(WorkOrderStatus.NEW, WorkOrderStatus.ASSIGNED,
                        dispatcher, 3L));
    }

    @Test
    void rejectsIllegalTransitionFromNewToCompleted() {
        UserPrincipal dispatcher = principal("DISPATCHER", 1L);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.validateTransition(WorkOrderStatus.NEW, WorkOrderStatus.COMPLETED,
                        dispatcher, 3L));
        assertTrue(ex.getMessage().contains("Illegal transition"));
    }

    @Test
    void rejectsCloseByTechnician() {
        UserPrincipal technician = principal("TECHNICIAN", 3L);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.validateTransition(WorkOrderStatus.COMPLETED, WorkOrderStatus.CLOSED,
                        technician, 3L));
        assertTrue(ex.getMessage().contains("Only managers can close"));
    }

    @Test
    void rejectsTransitionFromTerminalState() {
        UserPrincipal manager = principal("MANAGER", 1L);
        assertThrows(BusinessException.class, () ->
                stateMachine.validateTransition(WorkOrderStatus.CLOSED, WorkOrderStatus.NEW,
                        manager, null));
    }

    private UserPrincipal principal(String role, Long id) {
        return new UserPrincipal(com.keystone.domain.entity.User.builder()
                .id(id)
                .email("test@example.com")
                .passwordHash("hash")
                .fullName("Test User")
                .role(com.keystone.domain.enums.Role.valueOf(role))
                .active(true)
                .build());
    }
}
