package com.assetmanagement.assetservice.service.client;

import com.assetmanagement.assetservice.dto.AssetLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LogService")
public interface LogServiceClient {

    @PostMapping("/api/v1/logs/assets")
    void createLog(@RequestBody AssetLogRequest request);
}