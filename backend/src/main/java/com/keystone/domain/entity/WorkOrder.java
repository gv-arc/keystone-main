//package com.keystone.domain.entity;
//
//import com.keystone.domain.enums.Priority;
//import com.keystone.domain.enums.SlaStatus;
//import com.keystone.domain.enums.WorkOrderStatus;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "work_orders")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class WorkOrder {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, length = 255)
//    private String code;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Priority priority;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private WorkOrderStatus status;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "customer_id", nullable = false)
//    private Customer customer;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "site_id", nullable = false)
//    private Site site;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assignee_id")
//    private User assignee;
//
//    @Column(name = "sla_due_at")
//    private Instant slaDueAt;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "sla_status", nullable = false)
//    private SlaStatus slaStatus = SlaStatus.ON_TRACK;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "created_by_id", nullable = false)
//    private User createdBy;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private Instant updatedAt;
//
//    @Column(name = "closed_at")
//    private Instant closedAt;
//
//    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderBy("changedAt ASC")
//    @Builder.Default
//    private List<WorkOrderStatusHistory> statusHistory = new ArrayList<>();
//
//    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<PartUsage> partUsages = new ArrayList<>();
//
//    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<TimeLog> timeLogs = new ArrayList<>();
//
//    @PrePersist
//    void onCreate() {
//        Instant now = Instant.now();
//        createdAt = now;
//        updatedAt = now;
//    }
//
//    @PreUpdate
//    void onUpdate() {
//        updatedAt = Instant.now();
//    }
//
//    public boolean isEditable() {
//        return !status.isTerminal();
//    }
//
//    public BigDecimal getTotalPartsCost() {
//        return partUsages.stream()
//                .map(u -> u.getUnitCost().multiply(BigDecimal.valueOf(u.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//
//    public int getTotalMinutesLogged() {
//        return timeLogs.stream().mapToInt(TimeLog::getMinutes).sum();
//    }
//}



package com.keystone.domain.entity;

import com.keystone.domain.enums.Priority;
import com.keystone.domain.enums.SlaStatus;
import com.keystone.domain.enums.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255, columnDefinition = "varchar(255)")
    private String code;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_status", nullable = false)
    private SlaStatus slaStatus = SlaStatus.ON_TRACK;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    @Builder.Default
    private List<WorkOrderStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PartUsage> partUsages = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeLog> timeLogs = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isEditable() {
        return !status.isTerminal();
    }

    public BigDecimal getTotalPartsCost() {
        return partUsages.stream()
                .map(u -> u.getUnitCost().multiply(BigDecimal.valueOf(u.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalMinutesLogged() {
        return timeLogs.stream().mapToInt(TimeLog::getMinutes).sum();
    }
}
