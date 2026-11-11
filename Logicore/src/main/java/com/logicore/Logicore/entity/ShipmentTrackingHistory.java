
package com.logicore.Logicore.entity;

import com.logicore.Logicore.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_tracking_history")
@Getter
@Setter
public class ShipmentTrackingHistory {

    // Har tracking history record ki unique ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ek shipment ke multiple tracking history records ho sakte hain
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    // Shipment ka previous status store karna
    // Initial event ke liye null ho sakta hai
    @Enumerated(EnumType.STRING)
    @Column
    private ShipmentStatus previousStatus;

    // Shipment ka new status store karna
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus newStatus;

    // Status change ka reason ya additional information
    @Column(length = 500)
    private String remarks;

    // Status change ka date aur time
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // Record save hone se pehle timestamp automatically set karna
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
