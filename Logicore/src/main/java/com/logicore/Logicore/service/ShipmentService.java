package com.logicore.Logicore.service;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.dto.ShipmentResponseDTO;
import com.logicore.Logicore.entity.Shipment;
import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import com.logicore.Logicore.exception.DuplicateResourceException;
import com.logicore.Logicore.exception.InvalidStatusTransitionException;
import com.logicore.Logicore.exception.ResourceNotFoundException;
import com.logicore.Logicore.mapper.ShipmentMapper;
import com.logicore.Logicore.repository.ShipmentRepository;
import com.logicore.Logicore.specification.ShipmentSpecification;
import com.logicore.Logicore.util.ExcelHelper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final ExcelHelper excelHelper;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentMapper shipmentMapper, ExcelHelper excelHelper) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentMapper = shipmentMapper;
        this.excelHelper = excelHelper;
    }
//Create Shipment
    public ShipmentResponseDTO createShipment(ShipmentRequestDTO requestDTO) {
        if(shipmentRepository.existsByTrackingNumber(requestDTO.getTrackingNumber())){
            throw new DuplicateResourceException("Tracking number already exists!");
        }
        Shipment shipment = shipmentMapper.toEntity(requestDTO);
        shipment.setStatus(ShipmentStatus.CREATED);
        Shipment savedShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toDto(savedShipment);
    }
    //Get All Shipment
    public List<ShipmentResponseDTO> getAllShipments(){
        return shipmentRepository.findAll()
                .stream()
                .map(shipment -> shipmentMapper.toDto(shipment))
                .toList();
    }
    //Get Shipment By id
    public ShipmentResponseDTO getShipmentById(Long id){
        Shipment shipment = shipmentRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("Shipment Not Found!"));
        return shipmentMapper.toDto(shipment);
    }
    //Update Shipment By id
    public ShipmentResponseDTO updateShipment(Long id , ShipmentRequestDTO requestDTO){
        Shipment shipment =shipmentRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Shipment Not Found!")
        );
        if(shipmentRepository.existsByTrackingNumberAndIdNot(requestDTO.getTrackingNumber(),id)){
            throw new DuplicateResourceException("Tracking number already exists!");
        }

        shipment.setTrackingNumber(requestDTO.getTrackingNumber());
        shipment.setOrigin(requestDTO.getOrigin());
        shipment.setDestination(requestDTO.getDestination());
        shipment.setPriority(requestDTO.getPriority());

        Shipment updatedShipment = shipmentRepository.save(shipment);

        return shipmentMapper.toDto(updatedShipment);
    }
    //Delete Shipment
    public void deleteShipment(Long id){
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Shipment not found !"));
        shipmentRepository.delete(shipment);
    }
    //Update Shipment Status
    public ShipmentResponseDTO updateShipmentStatus(
            Long id,
            ShipmentStatus newStatus
    ){
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Shipment not found !"));

        ShipmentStatus currentStatus = shipment.getStatus();

        if(!isValidTransition(currentStatus,newStatus)){
            throw new InvalidStatusTransitionException("Invalid shipment status transition !");
        }
        shipment.setStatus(newStatus);

        Shipment updatedStatus = shipmentRepository.save(shipment);

        return shipmentMapper.toDto(updatedStatus);
    }
    //Status transition
    private boolean isValidTransition(
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus
    ){
        switch(currentStatus) {
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
            case DELIVERED:
            case CANCELLED:
                return false;

            default:
                return false;
        }
        }
    public Page<ShipmentResponseDTO> getAllShipment(Pageable pageable ){
        return shipmentRepository.findAll(pageable)
                .map(shipmentMapper::toDto);
    }
    // Excel se aaye DTOs mein duplicate tracking numbers check karna
    public void validateDuplicateShipments(List<ShipmentRequestDTO> shipmentList) {

        // Same Excel file ke andar duplicates check karne ke liye Set
        Set<String> trackingNumbers = new HashSet<>();

        for (ShipmentRequestDTO dto : shipmentList) {

            String trackingNumber = dto.getTrackingNumber();

            // Excel ke andar duplicate tracking number check
            if (!trackingNumbers.add(trackingNumber)) {
                throw new DuplicateResourceException(
                        "Duplicate tracking number in Excel: " + trackingNumber
                );
            }

            // Database mein pehle se tracking number exist karta hai ya nahi
            if (shipmentRepository.existsByTrackingNumber(trackingNumber)) {
                throw new DuplicateResourceException(
                        "Tracking number already exists: " + trackingNumber
                );
            }
        }
    }
    @Transactional
   public List<ShipmentResponseDTO> importShipments(
            List<ShipmentRequestDTO> shipmentList
    ){
        validateDuplicateShipments(shipmentList);
    List<Shipment> shipments = shipmentList.stream()
            .map(requestDTO -> {
                Shipment shipment = shipmentMapper.toEntity(requestDTO);

                shipment.setStatus(ShipmentStatus.CREATED);
                return shipment;
            })
            .toList();

    return shipmentRepository.saveAll(shipments)
            .stream()
            .map(shipmentMapper::toDto)
            .toList();
    }
    // Priority ke basis par shipments find karna with pagination
    public Page<ShipmentResponseDTO> findByPriority(
            ShipmentPriority priority,
            Pageable pageable
    ){
        return shipmentRepository.findByPriority(priority,pageable)
                .map(shipmentMapper::toDto);
    }
    // Origin ke basis par shipments find karna with pagination
    public Page<ShipmentResponseDTO> findByOrigin(
            String origin,
            Pageable pageable
    ){
        return shipmentRepository.findByOriginContainingIgnoreCase(origin,pageable)
                .map(shipmentMapper::toDto);
    }
    // Multiple filters ke basis par shipments search karna with pagination
    public Page<ShipmentResponseDTO> searchShipments(
            ShipmentPriority priority,
            ShipmentStatus status,
            String origin,
            Pageable pageable
    ) {

        // Specification class se combined dynamic conditions lena
        Specification<Shipment> specification =
                ShipmentSpecification.getShipmentsByFilters(
                        priority,
                        status,
                        origin
                );

        // Database se matching shipments fetch karke DTO mein convert karna
        return shipmentRepository.findAll(specification, pageable)
                .map(shipmentMapper::toDto);
    }

}
