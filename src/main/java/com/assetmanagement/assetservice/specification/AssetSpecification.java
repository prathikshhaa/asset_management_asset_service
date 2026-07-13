package com.assetmanagement.assetservice.specification;

import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import org.springframework.data.jpa.domain.Specification;

public class AssetSpecification {

    private AssetSpecification() {
    }

    public static Specification<Asset> filter(
            String assetName,
            String assetType,
            AssetStatus status,
            String serialNumber
    ) {

        return (root, query, cb) -> {

            var predicate = cb.conjunction();

            if (assetName != null && !assetName.isBlank()) {
                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("assetName")),
                                "%" + assetName.trim().toLowerCase() + "%"
                        )
                );
            }

            if (assetType != null && !assetType.isBlank()) {
                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("assetType")),
                                "%" + assetType.trim().toLowerCase() + "%"
                        )
                );
            }

            if (status != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("status"), status)
                );
            }

            if (serialNumber != null && !serialNumber.isBlank()) {
                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("serialNumber")),
                                "%" + serialNumber.trim().toLowerCase() + "%"
                        )
                );
            }

            return predicate;
        };
    }
}