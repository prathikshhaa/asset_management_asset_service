package com.assetmanagement.assetservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assets")
@org.hibernate.annotations.SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false)
    private String assetType;

    private String brand;

    private String model;

    private String manufacturer;

    @Column(length = 1000)
    private String description;

    private String department;

    private String location;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    @Column(precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    private String lastModifiedBy;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = AssetStatus.AVAILABLE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}