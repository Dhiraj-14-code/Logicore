package com.logicore.Logicore.dto;

import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import jakarta.validation.constraints.NegativeOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDTO {
    private Long id;

    private String trackingNumber;

    private String origin;

    private String destination;

    private ShipmentStatus status;

    private ShipmentPriority priority;
}
