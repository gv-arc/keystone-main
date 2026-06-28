package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private Instant createdAt;
}
