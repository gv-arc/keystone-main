package com.keystone.repository;

import com.keystone.domain.entity.WorkOrder;
import com.keystone.domain.enums.SlaStatus;
import com.keystone.domain.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByCode(String code);

    @Query("SELECT wo FROM WorkOrder wo " +
            "LEFT JOIN FETCH wo.customer " +
            "LEFT JOIN FETCH wo.site " +
            "LEFT JOIN FETCH wo.assignee " +
            "WHERE wo.id = :id")
    Optional<WorkOrder> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT wo.* FROM work_orders wo WHERE " +
            "(:customerId IS NULL OR wo.customer_id = :customerId) AND " +
            "(:assigneeId IS NULL OR wo.assignee_id = :assigneeId) AND " +
            "(:status IS NULL OR wo.status = CAST(:status AS VARCHAR)) AND " +
            "(:search IS NULL OR LOWER(wo.title) LIKE LOWER('%' || CAST(CAST(:search AS text) AS varchar) || '%') " +
            "OR LOWER(wo.code) LIKE LOWER('%' || CAST(CAST(:search AS text) AS varchar) || '%')) AND " +
            "(:openOnly = false OR wo.status NOT IN ('CLOSED', 'CANCELLED'))",
            countQuery = "SELECT COUNT(*) FROM work_orders wo WHERE " +
                    "(:customerId IS NULL OR wo.customer_id = :customerId) AND " +
                    "(:assigneeId IS NULL OR wo.assignee_id = :assigneeId) AND " +
                    "(:status IS NULL OR wo.status = CAST(:status AS VARCHAR)) AND " +
                    "(:search IS NULL OR LOWER(wo.title) LIKE LOWER('%' || CAST(CAST(:search AS text) AS varchar) || '%') " +
                    "OR LOWER(wo.code) LIKE LOWER('%' || CAST(CAST(:search AS text) AS varchar) || '%')) AND " +
                    "(:openOnly = false OR wo.status NOT IN ('CLOSED', 'CANCELLED'))",
            nativeQuery = true)
    Page<WorkOrder> search(@Param("customerId") Long customerId,
                           @Param("assigneeId") Long assigneeId,
                           @Param("status") String status,
                           @Param("search") String search,
                           @Param("openOnly") boolean openOnly,
                           Pageable pageable);

    List<WorkOrder> findByStatusNotIn(List<WorkOrderStatus> terminalStatuses);

    List<WorkOrder> findBySlaStatusInAndStatusNotIn(List<SlaStatus> slaStatuses,
                                                    List<WorkOrderStatus> terminalStatuses);

    long countByStatus(WorkOrderStatus status);

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.slaDueAt < :now " +
            "AND wo.status NOT IN ('CLOSED', 'CANCELLED')")
    long countOverdue(@Param("now") Instant now);

    @Query("SELECT wo.assignee.id, wo.assignee.fullName, COUNT(wo) FROM WorkOrder wo " +
            "WHERE wo.assignee IS NOT NULL AND wo.status NOT IN ('CLOSED', 'CANCELLED') " +
            "GROUP BY wo.assignee.id, wo.assignee.fullName")
    List<Object[]> countOpenByTechnician();

    @Query("SELECT wo.site.id, wo.site.name, COUNT(wo) FROM WorkOrder wo " +
            "WHERE wo.status NOT IN ('CLOSED', 'CANCELLED') " +
            "GROUP BY wo.site.id, wo.site.name")
    List<Object[]> countOpenBySite();
}