package com.logicore.Logicore.repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.logicore.Logicore.entity.Shipment;
import com.logicore.Logicore.enums.ShipmentPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> ,JpaSpecificationExecutor<Shipment>{
    boolean existsByTrackingNumber(String trackingNumber );
    boolean existsByTrackingNumberAndIdNot(String trackingNumber , Long id);

    Page<Shipment> findByPriority(
            ShipmentPriority priority,
            Pageable pageable
    );
    // Origin me given text search karna, case ignore karke
    Page<Shipment> findByOriginContainingIgnoreCase(
            String origin,
            Pageable pageable
    );
}
