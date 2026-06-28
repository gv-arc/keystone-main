package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SiteResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String name;
    private String address;
    private String city;
    private String postcode;
    private Instant createdAt;
}
