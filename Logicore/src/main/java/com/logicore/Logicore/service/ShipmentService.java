package com.logicore.Logicore.service;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.dto.ShipmentResponseDTO;
import com.logicore.Logicore.entity.Shipment;
import com.logicore.Logicore.enums.ShipmentStatus;
import com.logicore.Logicore.exception.DuplicateResourceException;
import com.logicore.Logicore.exception.InvalidStatusTransitionException;
import com.logicore.Logicore.exception.ResourceNotFoundException;
import com.logicore.Logicore.mapper.ShipmentMapper;
import com.logicore.Logicore.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentMapper shipmentMapper) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentMapper = shipmentMapper;
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
}
