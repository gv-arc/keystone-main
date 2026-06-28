package com.keystone.service;

import com.keystone.domain.entity.Part;
import com.keystone.dto.request.PartRequest;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.PartResponse;
import com.keystone.exception.BusinessException;
import com.keystone.exception.ResourceNotFoundException;
import com.keystone.mapper.EntityMapper;
import com.keystone.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<PartResponse> list(String search, Pageable pageable) {
        Page<Part> page = partRepository.search(search, pageable);
        return PageResponse.<PartResponse>builder()
                .content(page.getContent().stream().map(mapper::toPartResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional
    public PartResponse create(PartRequest request) {
        if (partRepository.findAll().stream().anyMatch(p -> p.getSku().equals(request.getSku()))) {
            throw new BusinessException("SKU already exists", HttpStatus.CONFLICT);
        }
        Part part = Part.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .unitCost(request.getUnitCost())
                .stockQuantity(request.getStockQuantity())
                .build();
        return mapper.toPartResponse(partRepository.save(part));
    }

    @Transactional
    public PartResponse update(Long id, PartRequest request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.setName(request.getName());
        part.setDescription(request.getDescription());
        part.setUnitCost(request.getUnitCost());
        part.setStockQuantity(request.getStockQuantity());
        return mapper.toPartResponse(partRepository.save(part));
    }
}
