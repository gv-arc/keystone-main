package com.keystone.repository;

import com.keystone.domain.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByCustomerId(Long customerId);

    Page<Site> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT s FROM Site s WHERE s.customer.id = :customerId AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Site> searchByCustomer(@Param("customerId") Long customerId,
                                @Param("search") String search,
                                Pageable pageable);
}
