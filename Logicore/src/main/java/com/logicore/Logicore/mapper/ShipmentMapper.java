package com.logicore.Logicore.mapper;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.dto.ShipmentResponseDTO;
import com.logicore.Logicore.entity.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    //dto yo entity
    public Shipment toEntity(ShipmentRequestDTO requestDTO){
        Shipment shipment = new Shipment();

        shipment.setTrackingNumber(requestDTO.getTrackingNumber());
        shipment.setOrigin(requestDTO.getOrigin());
        shipment.setDestination(requestDTO.getDestination());
        shipment.setPriority(requestDTO.getPriority());

        return shipment;
    }
    //entity to dto
    public ShipmentResponseDTO toDto(Shipment shipment){
        ShipmentResponseDTO responseDTO=new ShipmentResponseDTO();

        responseDTO.setId(shipment.getId());
        responseDTO.setTrackingNumber(shipment.getTrackingNumber());
        responseDTO.setOrigin(shipment.getOrigin());
        responseDTO.setDestination(shipment.getDestination());
        responseDTO.setStatus(shipment.getStatus());
        responseDTO.setPriority(shipment.getPriority());

        return responseDTO;
    }
}

