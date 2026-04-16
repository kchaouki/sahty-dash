package com.sahty.fs.repository.pharmacy;

import com.sahty.fs.entity.pharmacy.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    List<PurchaseOrder> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<PurchaseOrder> findByTenantIdAndStatus(String tenantId, PurchaseOrder.PurchaseOrderStatus status);
}
