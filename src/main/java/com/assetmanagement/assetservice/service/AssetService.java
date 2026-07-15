package com.assetmanagement.assetservice.service;
import java.io.InputStream;
import com.assetmanagement.assetservice.dto.*;
import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import com.assetmanagement.assetservice.exception.DuplicateAssetException;
import com.assetmanagement.assetservice.exception.FileProcessingException;
import com.assetmanagement.assetservice.exception.ResourceNotFoundException;
import com.assetmanagement.assetservice.repository.AssetRepository;
import com.assetmanagement.assetservice.service.client.LogServiceClient;
import com.assetmanagement.assetservice.specification.AssetSpecification;
import com.assetmanagement.assetservice.util.AssetMapper;
import com.assetmanagement.assetservice.util.ExcelAssetParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.assetmanagement.assetservice.dto.AssetLogRequest;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AssetService {

    private final FileStorageService fileStorageService;
private final LogServiceClient logServiceClient;
    private final AssetRepository assetRepository;

    private void validateAsset(AssetRequestDTO requestDTO){
        if (requestDTO.getPurchaseDate() != null &&
                requestDTO.getWarrantyExpiry() != null &&
                requestDTO.getWarrantyExpiry().isBefore(requestDTO.getPurchaseDate())) {

            throw new IllegalArgumentException(
                    "Warranty expiry date cannot be before purchase date.");
        }

        if (requestDTO.getPurchasePrice() != null &&
                requestDTO.getPurchasePrice().signum() < 0) {

            throw new IllegalArgumentException(
                    "Purchase price cannot be negative.");
        }
    }


    @Transactional
    public AssetResponseDTO createAsset(AssetRequestDTO requestDTO,
                                        MultipartFile image) {

        if (assetRepository.existsBySerialNumber(requestDTO.getSerialNumber())) {
            throw new DuplicateAssetException(
                    "Asset with Serial Number " +
                            requestDTO.getSerialNumber() +
                            " already exists");
        }

        validateAsset(requestDTO);

        Asset asset = AssetMapper.toEntity(requestDTO);

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.store(image);
            asset.setImageUrl(imageUrl);
        }


        Asset saved = assetRepository.save(asset);
        sendAssetLog(
                saved.getId(),
                "ASSET_CREATED",
                "Asset created successfully");
        return AssetMapper.toResponseDTO(saved);
    }
    public Page<AssetResponseDTO> getAllAssets(int page,
                                               int size,
                                               String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy));

        return assetRepository.findAll(pageable)
                .map(AssetMapper::toResponseDTO);
    }

    public AssetResponseDTO getAssetById(UUID id) {

        Asset asset = findAssetOrThrow(id);

        return AssetMapper.toResponseDTO(asset);
    }

    public Page<AssetResponseDTO> getAssetsByStatus(String statusText, int page, int size, String sortBy) {
        AssetStatus status = parseStatus(statusText);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assetRepository.findByStatus(status, pageable)
                .map(AssetMapper::toResponseDTO);
    }

    private AssetStatus parseStatus(String statusText) {
        if (!StringUtils.hasText(statusText)) {
            throw new IllegalArgumentException("Status must not be empty.");
        }
        try {
            return AssetStatus.valueOf(statusText.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid status '" + statusText + "'. Allowed values: AVAILABLE, ASSIGNED, MAINTENANCE");
        }
    }

    public Page<AssetResponseDTO> getAssetsByType(String type, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assetRepository.findByAssetType(type, pageable)
                .map(AssetMapper::toResponseDTO);
    }

    public Page<AssetResponseDTO> searchAssets(
            String keyword,
            int page,
            int size,
            String sortBy) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortBy));

        return assetRepository
                .findByAssetNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrManufacturerContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        keyword,
                        keyword,
                        pageable)
                .map(AssetMapper::toResponseDTO);
    }

    @Transactional
    public AssetResponseDTO updateAsset(UUID id,
                                        AssetRequestDTO requestDTO) {

        Asset existing = findAssetOrThrow(id);

        if (existing.isDeleted()) {
            throw new IllegalArgumentException(
                    "Deleted assets cannot be updated.");
        }

        validateAsset(requestDTO);

        AssetMapper.updateEntity(existing, requestDTO);


        existing.setLastModifiedBy(currentCaller());

        Asset updated = assetRepository.save(existing);
        sendAssetLog(
                updated.getId(),
                "ASSET_UPDATED",
                "Asset updated successfully");
        return AssetMapper.toResponseDTO(updated);
    }

    @Transactional
    public AssetResponseDTO patchAsset(UUID id,
                                       AssetUpdateDTO updateDTO) {
        Asset existing = findAssetOrThrow(id);

        if (existing.isDeleted()) {
            throw new IllegalArgumentException("Deleted assets cannot be updated.");
        }

        AssetMapper.patchEntity(existing, updateDTO);

        existing.setLastModifiedBy(currentCaller());

        Asset updated = assetRepository.save(existing);
        sendAssetLog(
                updated.getId(),
                "ASSET_UPDATED",
                "Asset details updated"
        );
        return AssetMapper.toResponseDTO(updated);
    }

    @Transactional
    public AssetResponseDTO updateAssetStatus(UUID id, AssetStatusUpdateDTO statusUpdateDTO) {

        Asset existing = findAssetOrThrow(id);

        AssetStatus previousStatus = existing.getStatus();
        AssetStatus newStatus = statusUpdateDTO.getStatus();

        if (previousStatus == newStatus) {
            throw new IllegalArgumentException(
                    "Asset is already in " + newStatus + " status.");
        }

        switch (previousStatus) {

            case AVAILABLE -> {
                if(newStatus != AssetStatus.ASSIGNED &&
                        newStatus != AssetStatus.MAINTENANCE) {
                    throw new IllegalArgumentException(
                            "AVAILABLE assets can only be changed to ASSIGNED or MAINTENANCE.");
                }
            }

            case ASSIGNED -> {
                if (newStatus != AssetStatus.AVAILABLE &&
                        newStatus != AssetStatus.MAINTENANCE) {
                    throw new IllegalArgumentException(
                            "ASSIGNED assets can only be changed to AVAILABLE or MAINTENANCE.");
                }
            }

            case MAINTENANCE -> {
                if (newStatus != AssetStatus.AVAILABLE) {
                    throw new IllegalArgumentException(
                            "MAINTENANCE assets can only be changed to AVAILABLE.");
                }
            }
        }

        existing.setStatus(newStatus);
        existing.setLastModifiedBy(currentCaller());

        Asset updated = assetRepository.save(existing);
        sendAssetLog(
                updated.getId(),
                "STATUS_UPDATED",
                "Status changed to " + updated.getStatus());
        return AssetMapper.toResponseDTO(updated);
    }

    private String currentCaller() {

        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null
                && authentication.getName() != null
                && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }

        return "unknown";
    }

    @Transactional
    public void deleteAsset(UUID id) {

        Asset existing = findAssetOrThrow(id);

        if (existing.isDeleted()) {

            throw new IllegalArgumentException(
                    "Asset is already deleted.");
        }

        if (existing.getStatus() == AssetStatus.ASSIGNED) {

            throw new IllegalArgumentException(
                    "Assigned assets cannot be deleted.");
        }

        if (existing.getStatus() == AssetStatus.MAINTENANCE) {

            throw new IllegalArgumentException(
                    "Assets under maintenance cannot be deleted.");
        }

        existing.setDeleted(true);
        existing.setDeletedAt(java.time.LocalDateTime.now());
        existing.setLastModifiedBy(currentCaller());

        assetRepository.save(existing);
        sendAssetLog(
                existing.getId(),
                "ASSET_DELETED",
                "Asset deleted");
    }
    public DashboardSummaryDTO getDashboardSummary() {

        long totalAssets = assetRepository.count();

        long deletedAssets =
                assetRepository.findByDeletedTrue().size();

        long availableAssets =
                assetRepository.countByStatusAndDeletedFalse(
                        AssetStatus.AVAILABLE);

        long assignedAssets =
                assetRepository.countByStatusAndDeletedFalse(
                        AssetStatus.ASSIGNED);

        long maintenanceAssets =
                assetRepository.countByStatusAndDeletedFalse(
                        AssetStatus.MAINTENANCE);

        Map<String, Long> countByStatus = new HashMap<>();

        for (AssetStatus status : AssetStatus.values()) {

            countByStatus.put(
                    status.name(),
                    assetRepository.countByStatusAndDeletedFalse(status));
        }

        Map<String, Long> countByType = new HashMap<>();

        for (String type : assetRepository.findDistinctAssetTypes()) {

            countByType.put(
                    type,
                    assetRepository.countByAssetType(type));
        }

        List<AssetResponseDTO> latestAssets =
                assetRepository.findTop5ByDeletedFalseOrderByCreatedAtDesc()
                        .stream()
                        .map(AssetMapper::toResponseDTO)
                        .toList();

        return DashboardSummaryDTO.builder()

                .totalAssets(totalAssets)

                .availableAssets(availableAssets)

                .assignedAssets(assignedAssets)

                .maintenanceAssets(maintenanceAssets)

                .deletedAssets(deletedAssets)

                .countByStatus(countByStatus)

                .countByType(countByType)

                .totalAssetTypes(countByType.size())

                .latestAssets(latestAssets)

                .build();
    }

    public BulkUploadResultDTO bulkCreateAssets(List<AssetRequestDTO> requests) {

        List<AssetResponseDTO> created = new ArrayList<>();
        List<BulkUploadResultDTO.RowError> errors = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();

        for (int i = 0; i < requests.size(); i++) {
            int rowNumber = i + 1;
            AssetRequestDTO dto = requests.get(i);

            String validationError = validateBulkRow(dto, seenInBatch);
            if (validationError != null) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(rowNumber)
                        .serialNumber(dto != null ? dto.getSerialNumber() : null)
                        .reason(validationError)
                        .build());
                continue;
            }

            try {
                seenInBatch.add(dto.getSerialNumber());
                created.add(createAsset(dto, null));
            } catch (DuplicateAssetException ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(rowNumber)
                        .serialNumber(dto.getSerialNumber())
                        .reason(ex.getMessage())
                        .build());
            } catch (Exception ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(rowNumber)
                        .serialNumber(dto.getSerialNumber())
                        .reason("Failed to save: " + ex.getMessage())
                        .build());
            }
        }

        return BulkUploadResultDTO.builder()
                .totalReceived(requests.size())
                .successCount(created.size())
                .failureCount(errors.size())
                .createdAssets(created)
                .errors(errors)
                .build();
    }
    @Transactional
    public BulkUploadResultDTO bulkCreateAssetsFromExcel(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty.");
        }

        List<AssetResponseDTO> created = new ArrayList<>();
        List<BulkUploadResultDTO.RowError> errors = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();

        List<ExcelAssetParser.ExcelRow> rows;

        try (InputStream inputStream = file.getInputStream()) {
            rows = ExcelAssetParser.parse(inputStream);
        } catch (IOException ex) {
            throw new FileProcessingException(
                    "Unable to read uploaded file: " + ex.getMessage(), ex);
        }

        for (ExcelAssetParser.ExcelRow row : rows) {

            if (row.parseError() != null) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .reason(row.parseError())
                        .build());
                continue;
            }

            AssetRequestDTO dto = row.dto();

            String validationError = validateBulkRow(dto, seenInBatch);

            if (validationError != null) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .serialNumber(dto != null ? dto.getSerialNumber() : null)
                        .reason(validationError)
                        .build());
                continue;
            }

            try {
                seenInBatch.add(dto.getSerialNumber());
                created.add(createAsset(dto, null));

            } catch (DuplicateAssetException ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .serialNumber(dto.getSerialNumber())
                        .reason(ex.getMessage())
                        .build());

            } catch (Exception ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .serialNumber(dto.getSerialNumber())
                        .reason("Failed to save: " + ex.getMessage())
                        .build());
            }
        }

        return BulkUploadResultDTO.builder()
                .totalReceived(rows.size())
                .successCount(created.size())
                .failureCount(errors.size())
                .createdAssets(created)
                .errors(errors)
                .build();
    }

    private String validateBulkRow(AssetRequestDTO dto, Set<String> seenInBatch) {
        if (dto == null) {
            return "Row could not be parsed.";
        }
        if (!StringUtils.hasText(dto.getAssetName())) {
            return "assetName is required.";
        }
        if (!StringUtils.hasText(dto.getAssetType())) {
            return "assetType is required.";
        }
        if (!StringUtils.hasText(dto.getSerialNumber())) {
            return "serialNumber is required.";
        }
        if (!StringUtils.hasText(dto.getBrand())) {
            return "brand is required.";
        }
        if (seenInBatch.contains(dto.getSerialNumber())) {
            return "Duplicate serial number within this upload: " + dto.getSerialNumber();
        }
        return null;
    }

    private Asset findAssetOrThrow(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }
    public List<AssetSummaryDTO> getAllAssetsSummary() {

        return assetRepository.findAll()
                .stream()
                .map(AssetMapper::toSummaryDTO)
                .toList();
    }

    public AssetSummaryDTO getAssetSummary(UUID id) {

        Asset asset = findAssetOrThrow(id);

        return AssetMapper.toSummaryDTO(asset);
    }
    public List<AssetResponseDTO> getDeletedAssets() {

        return assetRepository.findByDeletedTrue()
                .stream()
                .map(AssetMapper::toResponseDTO)
                .toList();

    }
    @Transactional
    public AssetResponseDTO restoreAsset(UUID id) {

        Asset asset = assetRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deleted asset not found with id : " + id));

        asset.setDeleted(false);
        asset.setDeletedAt(null);
        asset.setLastModifiedBy(currentCaller());

        Asset restored = assetRepository.save(asset);
        sendAssetLog(
                restored.getId(),
                "ASSET_RESTORED",
                "Asset restored");
        return AssetMapper.toResponseDTO(restored);
    }

    public Page<AssetResponseDTO> filterAssets(
            String assetName,
            String assetType,
            AssetStatus status,
            String serialNumber,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Asset> assets = assetRepository.findAll(
                AssetSpecification.filter(
                        assetName,
                        assetType,
                        status,
                        serialNumber),
                pageable);

        return assets.map(AssetMapper::toResponseDTO);
    }
    private void sendAssetLog(UUID assetId, String action, String details) {

        AssetLogRequest log = new AssetLogRequest();

        log.setAssetId(assetId);
        log.setAction(action);
        log.setPerformedBy(currentCaller());
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());

        logServiceClient.createLog(log);
    }
}