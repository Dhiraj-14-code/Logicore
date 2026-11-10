package com.logicore.Logicore.specification;

import com.logicore.Logicore.entity.Shipment;
import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;


public class ShipmentSpecification {

    // Priority ke basis par dynamic database condition create karna
    public static Specification<Shipment> hasPriority(
            ShipmentPriority priority
    ) {

        // Root = Shipment entity ke fields access karne ke liye
        // CriteriaBuilder = SQL conditions create karne ke liye
        return (root, query, criteriaBuilder) ->

                // SQL equivalent: WHERE priority = ?
                criteriaBuilder.equal(
                        root.get("priority"),
                        priority
                );
    }


        // Status ke basis par dynamic database condition create karna
        public static Specification<Shipment> hasStatus(
                ShipmentStatus status
        ) {

            // Root = Shipment entity ke fields access karne ke liye
            // CriteriaBuilder = SQL conditions create karne ke liye
            return (root, query, criteriaBuilder) ->

                    // SQL equivalent: WHERE priority = ?
                    criteriaBuilder.equal(
                            root.get("status"),
                            status
                    );
        }
    // Origin ke basis par partial aur case-insensitive search karna
    public static Specification<Shipment> hasOrigin(String origin) {

        // Lambda expression ke through dynamic database condition create karna
        return (root, query, criteriaBuilder) ->

                criteriaBuilder.like(

                        // Database ke origin field ko lowercase mein convert karna
                        criteriaBuilder.lower(root.get("origin")),

                        // User ke search input ko lowercase karna
                        // % ka matlab hai: search text ke pehle aur baad koi bhi characters ho sakte hain
                        "%" + origin.toLowerCase() + "%"
                );
    }
    // Sabhi optional filters ko dynamically combine karna
    public static Specification<Shipment> getShipmentsByFilters(
            ShipmentPriority priority,
            ShipmentStatus status,
            String origin
    ) {

        // Starting mein empty condition create karna
        Specification<Shipment> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        // Priority null nahi hai toh priority filter add karna
        if (priority != null) {
            specification = specification.and(hasPriority(priority));
        }

        // Status null nahi hai toh status filter add karna
        if (status != null) {
            specification = specification.and(hasStatus(status));
        }

        // Origin null ya blank nahi hai toh origin filter add karna
        if (origin != null && !origin.isBlank()) {
            specification = specification.and(hasOrigin(origin));
        }

        // Final combined specification return karna
        return specification;
    }
    }


