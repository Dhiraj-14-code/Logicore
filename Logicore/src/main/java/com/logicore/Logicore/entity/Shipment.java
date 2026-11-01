package com.logicore.Logicore.entity;

import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name ="shipment")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String trackingNumber;

    private String origin;

    private String destination;

    private ShipmentStatus status;

    private ShipmentPriority priority;
}
