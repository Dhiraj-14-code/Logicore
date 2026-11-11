
package com.logicore.Logicore.service;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.dto.ShipmentResponseDTO;
import com.logicore.Logicore.dto.ShipmentTrackingHistoryResponseDTO;
import com.logicore.Logicore.entity.Shipment;
import com.logicore.Logicore.entity.ShipmentTrackingHistory;
import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import com.logicore.Logicore.exception.DuplicateResourceException;
import com.logicore.Logicore.exception.InvalidStatusTransitionException;
import com.logicore.Logicore.exception.ResourceNotFoundException;
import com.logicore.Logicore.mapper.ShipmentMapper;
import com.logicore.Logicore.repository.ShipmentRepository;
import com.logicore.Logicore.repository.ShipmentTrackingHistoryRepository;
import com.logicore.Logicore.specification.ShipmentSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentTrackingHistoryRepository shipmentTrackingHistoryRepository;

    // Constructor injection: required dependencies inject karna
    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentMapper shipmentMapper,
            ShipmentTrackingHistoryRepository shipmentTrackingHistoryRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentMapper = shipmentMapper;
        this.shipmentTrackingHistoryRepository = shipmentTrackingHistoryRepository;
    }

    // =====================================================
    // CREATE SHIPMENT
    // =====================================================

    @Transactional
    public ShipmentResponseDTO createShipment(ShipmentRequestDTO requestDTO) {

        // Check karna ki tracking number pehle se exist karta hai ya nahi
        if (shipmentRepository.existsByTrackingNumber(requestDTO.getTrackingNumber())) {
            throw new DuplicateResourceException(
                    "Tracking number already exists!"
            );
        }

        // DTO ko Shipment entity mein convert karna
        Shipment shipment = shipmentMapper.toEntity(requestDTO);

        // Har new shipment ka initial status CREATED hoga
        shipment.setStatus(ShipmentStatus.CREATED);

        // Shipment database mein save karna
        Shipment savedShipment = shipmentRepository.save(shipment);

        // Initial tracking history record create karna
        ShipmentTrackingHistory history = new ShipmentTrackingHistory();

        history.setShipment(savedShipment);
        history.setPreviousStatus(null);
        history.setNewStatus(ShipmentStatus.CREATED);
        history.setRemarks("Shipment created");

        // Initial history database mein save karna
        shipmentTrackingHistoryRepository.save(history);

        // Saved shipment ko response DTO mein convert karna
        return shipmentMapper.toDto(savedShipment);
    }

    // =====================================================
    // GET ALL SHIPMENTS
    // =====================================================

    // Sabhi shipments retrieve karna
    public List<ShipmentResponseDTO> getAllShipments() {

        return shipmentRepository.findAll()
                .stream()
                .map(shipmentMapper::toDto)
                .toList();
    }

    // =====================================================
    // GET SHIPMENT BY ID
    // =====================================================

    public ShipmentResponseDTO getShipmentById(Long id) {

        // ID ke basis par shipment find karna
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shipment not found!")
                );

        return shipmentMapper.toDto(shipment);
    }

    // =====================================================
    // UPDATE SHIPMENT
    // =====================================================

    @Transactional
    public ShipmentResponseDTO updateShipment(
            Long id,
            ShipmentRequestDTO requestDTO
    ) {

        // Existing shipment fetch karna
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shipment not found!")
                );

        // Check karna ki tracking number kisi doosre shipment ka toh nahi
        if (shipmentRepository.existsByTrackingNumberAndIdNot(
                requestDTO.getTrackingNumber(), id)) {

            throw new DuplicateResourceException(
                    "Tracking number already exists!"
            );
        }

        // Allowed shipment fields update karna
        shipment.setTrackingNumber(requestDTO.getTrackingNumber());
        shipment.setOrigin(requestDTO.getOrigin());
        shipment.setDestination(requestDTO.getDestination());
        shipment.setPriority(requestDTO.getPriority());

        Shipment updatedShipment = shipmentRepository.save(shipment);

        return shipmentMapper.toDto(updatedShipment);
    }

    // =====================================================
    // DELETE SHIPMENT
    // =====================================================

    @Transactional
    public void deleteShipment(Long id) {

        // Shipment exist karta hai ya nahi check karna
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shipment not found!")
                );

        // Already deleted asel tar error dya
        if (shipment.isDeleted()) {
            throw new ResourceNotFoundException("Shipment already deleted");
        }

        // Actual delete na karta flag true kara
        shipment.setDeleted(true);

        // Updated shipment database madhye save kara
        shipmentRepository.save(shipment);

    }

    // =====================================================
    // UPDATE SHIPMENT STATUS + TRACKING HISTORY
    // =====================================================

    @Transactional
    public ShipmentResponseDTO updateShipmentStatus(
            Long id,
            ShipmentStatus newStatus
    ) {

        // Step 1: Existing shipment database se fetch karna
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shipment not found!")
                );

        // Step 2: Null status validation karna
        if (newStatus == null) {
            throw new InvalidStatusTransitionException(
                    "New shipment status cannot be null!"
            );
        }

        // Step 3: Update karne se pehle previous status capture karna
        ShipmentStatus previousStatus = shipment.getStatus();

        // Step 4: Status transition valid hai ya nahi check karna
        if (!isValidTransition(previousStatus, newStatus)) {

            throw new InvalidStatusTransitionException(
                    "Invalid shipment status transition from "
                            + previousStatus + " to " + newStatus
            );
        }

        // Step 5: Shipment ka current status update karna
        shipment.setStatus(newStatus);

        // Step 6: Updated shipment database mein save karna
        Shipment updatedShipment = shipmentRepository.save(shipment);

        // Step 7: Tracking history ka naya object create karna
        ShipmentTrackingHistory history = new ShipmentTrackingHistory();

        // Step 8: History ko existing shipment ke saath associate karna
        history.setShipment(updatedShipment);

        // Step 9: Previous aur new status store karna
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);

        // Step 10: Status change ka description store karna
        history.setRemarks(
                "Shipment status changed from "
                        + previousStatus + " to " + newStatus
        );

        // Step 11: History database mein save karna
        // changedAt automatically @PrePersist se set hoga
        shipmentTrackingHistoryRepository.save(history);

        // Step 12: Updated shipment ka response return karna
        return shipmentMapper.toDto(updatedShipment);
    }

    // =====================================================
    // SHIPMENT STATUS TRANSITION VALIDATION
    // =====================================================

    private boolean isValidTransition(
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus
    ) {

        // Null status allowed nahi hai
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        // Shipment lifecycle ke according transition validate karna
        switch (currentStatus) {

            case CREATED:
                return newStatus == ShipmentStatus.PICKED_UP
                        || newStatus == ShipmentStatus.CANCELLED;

            case PICKED_UP:
                return newStatus == ShipmentStatus.IN_TRANSIT
                        || newStatus == ShipmentStatus.CANCELLED;

            case IN_TRANSIT:
                return newStatus == ShipmentStatus.OUT_FOR_DELIVERY
                        || newStatus == ShipmentStatus.CANCELLED;

            case OUT_FOR_DELIVERY:
                return newStatus == ShipmentStatus.DELIVERED;

            // Delivered aur cancelled terminal statuses hain
            case DELIVERED:
            case CANCELLED:
                return false;

            default:
                return false;
        }
    }

    // =====================================================
    // GET ALL SHIPMENTS WITH PAGINATION AND SORTING
    // =====================================================

    public Page<ShipmentResponseDTO> getAllShipment(Pageable pageable) {

        // Pageable ke according database se shipments fetch karna
        return shipmentRepository.findByDeletedFalse(pageable)
                .map(shipmentMapper::toDto);
    }

    // =====================================================
    // EXCEL BULK IMPORT - DUPLICATE VALIDATION
    // =====================================================

    // Excel file ke andar duplicate tracking numbers check karna
    private void validateDuplicateShipments(
            List<ShipmentRequestDTO> shipmentList
    ) {

        // Same Excel file ke duplicates detect karne ke liye Set
        Set<String> trackingNumbers = new HashSet<>();

        for (ShipmentRequestDTO dto : shipmentList) {

            String trackingNumber = dto.getTrackingNumber();

            // Excel file ke andar duplicate check karna
            if (!trackingNumbers.add(trackingNumber)) {

                throw new DuplicateResourceException(
                        "Duplicate tracking number in Excel: "
                                + trackingNumber
                );
            }

            // Database mein tracking number pehle se exist karta hai ya nahi
            if (shipmentRepository.existsByTrackingNumber(trackingNumber)) {

                throw new DuplicateResourceException(
                        "Tracking number already exists: "
                                + trackingNumber
                );
            }
        }
    }

    // =====================================================
    // EXCEL BULK SHIPMENT IMPORT
    // =====================================================

    @Transactional
    public List<ShipmentResponseDTO> importShipments(
            List<ShipmentRequestDTO> shipmentList
    ) {

        // Empty ya null Excel data reject karna
        if (shipmentList == null || shipmentList.isEmpty()) {
            throw new IllegalArgumentException(
                    "No shipment records found in Excel"
            );
        }

        // Duplicate tracking numbers validate karna
        validateDuplicateShipments(shipmentList);

        // Excel DTOs ko Shipment entities mein convert karna
        List<Shipment> shipments = shipmentList.stream()
                .map(requestDTO -> {

                    Shipment shipment = shipmentMapper.toEntity(requestDTO);

                    // Imported shipments ka initial status CREATED hoga
                    shipment.setStatus(ShipmentStatus.CREATED);

                    return shipment;
                })
                .toList();

        // Sabhi shipments database mein save karna
        List<Shipment> savedShipments =
                shipmentRepository.saveAll(shipments);

        // Har imported shipment ke liye initial tracking history create karna
        List<ShipmentTrackingHistory> histories = savedShipments.stream()
                .map(savedShipment -> {

                    ShipmentTrackingHistory history =
                            new ShipmentTrackingHistory();

                    history.setShipment(savedShipment);
                    history.setPreviousStatus(null);
                    history.setNewStatus(ShipmentStatus.CREATED);
                    history.setRemarks("Shipment imported from Excel");

                    return history;
                })
                .toList();

        // Tracking histories database mein save karna
        shipmentTrackingHistoryRepository.saveAll(histories);

        // Saved shipments ko response DTOs mein convert karna
        return savedShipments.stream()
                .map(shipmentMapper::toDto)
                .toList();
    }

    // =====================================================
    // SEARCH BY PRIORITY
    // =====================================================

    public Page<ShipmentResponseDTO> findByPriority(
            ShipmentPriority priority,
            Pageable pageable
    ) {

        // Priority ke basis par shipments fetch aur DTO mapping karna
        return shipmentRepository.findByPriority(priority, pageable)
                .map(shipmentMapper::toDto);
    }

    // =====================================================
    // SEARCH BY ORIGIN
    // =====================================================

    public Page<ShipmentResponseDTO> findByOrigin(
            String origin,
            Pageable pageable
    ) {

        // Origin ke basis par case-insensitive partial search karna
        return shipmentRepository.findByOriginContainingIgnoreCase(
                        origin,
                        pageable
                )
                .map(shipmentMapper::toDto);
    }

    // =====================================================
    // COMBINED DYNAMIC SEARCH AND FILTERING
    // =====================================================

    public Page<ShipmentResponseDTO> searchShipments(
            ShipmentPriority priority,
            ShipmentStatus status,
            String origin,
            Pageable pageable
    ) {

        // Optional filters ko combine karke Specification create karna
        Specification<Shipment> specification =
                ShipmentSpecification.getShipmentsByFilters(
                        priority,
                        status,
                        origin
                );

        // Matching shipments pagination aur sorting ke saath fetch karna
        return shipmentRepository.findAll(specification, pageable)
                .map(shipmentMapper::toDto);
    }


    public List<ShipmentTrackingHistoryResponseDTO> getShipmentTrackingHistory(Long shipmentId) {
        shipmentRepository.findById(shipmentId)
                .orElseThrow(()->new ResourceNotFoundException("Shipment not found!!"));

        List<ShipmentTrackingHistory> histories = shipmentTrackingHistoryRepository.findByShipmentIdOrderByChangedAtAsc(shipmentId);

        return histories.stream()
                .map(shipmentMapper::toTrackingHistoryDto)
                .toList();

    }
}