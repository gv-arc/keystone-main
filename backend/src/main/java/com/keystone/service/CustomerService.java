package com.keystone.service;

import com.keystone.domain.entity.Customer;
import com.keystone.domain.entity.Site;
import com.keystone.dto.request.CustomerRequest;
import com.keystone.dto.request.SiteRequest;
import com.keystone.dto.response.CustomerResponse;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.SiteResponse;
import com.keystone.exception.ResourceNotFoundException;
import com.keystone.mapper.EntityMapper;
import com.keystone.repository.CustomerRepository;
import com.keystone.repository.SiteRepository;
import com.keystone.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.keystone.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(String search, Pageable pageable, UserPrincipal actor) {
        if ("CUSTOMER".equals(actor.getRole())) {
            Customer customer = customerRepository.findById(actor.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            return PageResponse.<CustomerResponse>builder()
                    .content(java.util.List.of(mapper.toCustomerResponse(customer)))
                    .page(0)
                    .size(1)
                    .totalElements(1)
                    .totalPages(1)
                    .build();
        }
        Page<Customer> page = customerRepository.search(search, pageable);
        return toPage(page, mapper::toCustomerResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id, UserPrincipal actor) {
        Customer customer = findCustomer(id);
        assertCustomerAccess(customer, actor);
        return mapper.toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .build();
        return mapper.toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        customer.setName(request.getName());
        customer.setContactEmail(request.getContactEmail());
        customer.setContactPhone(request.getContactPhone());
        customer.setAddress(request.getAddress());
        return mapper.toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public PageResponse<SiteResponse> listSites(Long customerId, String search, Pageable pageable,
                                                UserPrincipal actor) {
        assertCustomerAccess(findCustomer(customerId), actor);
        Page<Site> page = siteRepository.searchByCustomer(customerId, search, pageable);
        return toPage(page, mapper::toSiteResponse);
    }

    @Transactional
    public SiteResponse createSite(Long customerId, SiteRequest request) {
        Customer customer = findCustomer(customerId);
        Site site = Site.builder()
                .customer(customer)
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .postcode(request.getPostcode())
                .build();
        return mapper.toSiteResponse(siteRepository.save(site));
    }

    @Transactional
    public SiteResponse updateSite(Long customerId, Long siteId, SiteRequest request) {
        Site site = findSite(siteId);
        if (!site.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("Site does not belong to customer", HttpStatus.BAD_REQUEST);
        }
        site.setName(request.getName());
        site.setAddress(request.getAddress());
        site.setCity(request.getCity());
        site.setPostcode(request.getPostcode());
        return mapper.toSiteResponse(siteRepository.save(site));
    }

    public Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public Site findSite(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found: " + id));
    }

    private void assertCustomerAccess(Customer customer, UserPrincipal actor) {
        if ("CUSTOMER".equals(actor.getRole()) && !customer.getId().equals(actor.getCustomerId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }
    }

    private <E, R> PageResponse<R> toPage(Page<E> page, java.util.function.Function<E, R> mapperFn) {
        return PageResponse.<R>builder()
                .content(page.getContent().stream().map(mapperFn).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
