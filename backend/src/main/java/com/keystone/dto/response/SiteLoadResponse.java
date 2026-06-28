package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SiteLoadResponse {
    private Long siteId;
    private String siteName;
    private long openCount;
}

