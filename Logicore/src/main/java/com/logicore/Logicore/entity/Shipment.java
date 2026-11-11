
package com.logicore.Logicore.entity;

import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "shipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tracking number unique hona chahiye.
    @Column(nullable = false, unique = true, length = 100)
    private String trackingNumber;

    // Origin required field hai.
    @Column(nullable = false, length = 255)
    private String origin;

    // Destination required field hai.
    @Column(nullable = false, length = 255)
    private String destination;

    // Enum ka naam database mein STRING ke form mein save hoga.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentPriority priority;

    // Soft delete flag: false = active, true = deleted
    @Column(nullable = false)
    private boolean deleted = false;
}
