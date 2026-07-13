package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.AssetStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetRequestDTO {

    @NotBlank(message = "Asset name is required")
    @Size(max = 100)
    private String assetName;

    @NotBlank(message = "Asset type is required")
    private String assetType;

    @NotBlank(message = "Brand is required")
    private String brand;

    private String model;

    private String manufacturer;

    private String description;

    private String department;

    private String location;

    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    @PositiveOrZero(message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    private AssetStatus status;
}