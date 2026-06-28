package com.keystone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiteRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private String city;
    private String postcode;
}
