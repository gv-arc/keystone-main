package com.keystone.service;

import com.keystone.domain.enums.Priority;
import com.keystone.domain.enums.Role;
import com.keystone.domain.enums.WorkOrderStatus;
import com.keystone.exception.BusinessException;
import com.keystone.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class WorkOrderStateMachine {

    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> TRANSITIONS = Map.of(
            WorkOrderStatus.NEW, EnumSet.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.ASSIGNED, EnumSet.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.IN_PROGRESS, EnumSet.of(WorkOrderStatus.ON_HOLD, WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.ON_HOLD, EnumSet.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.COMPLETED, EnumSet.of(WorkOrderStatus.CLOSED),
            WorkOrderStatus.CLOSED, EnumSet.noneOf(WorkOrderStatus.class),
            WorkOrderStatus.CANCELLED, EnumSet.noneOf(WorkOrderStatus.class)
    );

    public void validateTransition(WorkOrderStatus from, WorkOrderStatus to, UserPrincipal actor,
                                   Long assigneeId) {
        if (from.isTerminal()) {
            throw new BusinessException("Work order is in terminal state: " + from, HttpStatus.CONFLICT);
        }

        Set<WorkOrderStatus> allowed = TRANSITIONS.getOrDefault(from, EnumSet.noneOf(WorkOrderStatus.class));
        if (!allowed.contains(to)) {
            throw new BusinessException(
                    "Illegal transition from " + from + " to " + to, HttpStatus.CONFLICT);
        }

        validateRoleForTransition(from, to, actor, assigneeId);
    }

    private void validateRoleForTransition(WorkOrderStatus from, WorkOrderStatus to,
                                           UserPrincipal actor, Long assigneeId) {
        String role = actor.getRole();

        switch (to) {
            case ASSIGNED -> {
                if (!Role.DISPATCHER.name().equals(role) && !Role.MANAGER.name().equals(role)) {
                    throw new BusinessException("Only dispatchers can assign work orders", HttpStatus.FORBIDDEN);
                }
            }
            case IN_PROGRESS -> {
                if (from == WorkOrderStatus.ON_HOLD) {
                    if (!Role.TECHNICIAN.name().equals(role) || !actor.getId().equals(assigneeId)) {
                        throw new BusinessException("Only the assigned technician can resume work", HttpStatus.FORBIDDEN);
                    }
                } else if (!Role.TECHNICIAN.name().equals(role) || !actor.getId().equals(assigneeId)) {
                    throw new BusinessException("Only the assigned technician can start work", HttpStatus.FORBIDDEN);
                }
            }
            case ON_HOLD -> {
                if (!Role.TECHNICIAN.name().equals(role) || !actor.getId().equals(assigneeId)) {
                    throw new BusinessException("Only the assigned technician can hold work", HttpStatus.FORBIDDEN);
                }
            }
            case COMPLETED -> {
                if (!Role.TECHNICIAN.name().equals(role) || !actor.getId().equals(assigneeId)) {
                    throw new BusinessException("Only the assigned technician can complete work", HttpStatus.FORBIDDEN);
                }
            }
            case CLOSED -> {
                if (!Role.MANAGER.name().equals(role)) {
                    throw new BusinessException("Only managers can close work orders", HttpStatus.FORBIDDEN);
                }
            }
            case CANCELLED -> {
                if (!Role.MANAGER.name().equals(role) && !Role.DISPATCHER.name().equals(role)) {
                    throw new BusinessException("Only dispatchers or managers can cancel work orders", HttpStatus.FORBIDDEN);
                }
            }
            default -> {}
        }
    }
}
