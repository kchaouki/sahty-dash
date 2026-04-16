package com.sahty.fs.service.pharmacy;

import com.sahty.fs.entity.pharmacy.Product;
import com.sahty.fs.entity.pharmacy.StockItem;
import com.sahty.fs.entity.pharmacy.StockLocation;
import com.sahty.fs.entity.pharmacy.StockMovement;
import com.sahty.fs.repository.pharmacy.ProductRepository;
import com.sahty.fs.repository.pharmacy.StockItemRepository;
import com.sahty.fs.repository.pharmacy.StockLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyService {

    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final StockLocationRepository stockLocationRepository;

    // ---- Products ----

    public Page<Product> searchProducts(String tenantId, String query, int page, int size) {
        return productRepository.searchProducts(tenantId, query == null ? "" : query,
                PageRequest.of(page, size));
    }

    public List<Product> findAllProducts(String tenantId) {
        return productRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    public Optional<Product> findProductById(String id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(String id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setActive(false);
            productRepository.save(p);
        });
    }

    // ---- Stock Locations ----

    public List<StockLocation> findLocations(String tenantId) {
        return stockLocationRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    public List<StockLocation> findPharmacyLocations(String tenantId) {
        return stockLocationRepository.findByTenantIdAndScopeAndIsActiveTrue(tenantId, StockLocation.LocationScope.PHARMACY);
    }

    public List<StockLocation> findServiceLocations(String tenantId) {
        return stockLocationRepository.findByTenantIdAndScopeAndIsActiveTrue(tenantId, StockLocation.LocationScope.SERVICE);
    }

    @Transactional
    public StockLocation saveLocation(StockLocation location) {
        return stockLocationRepository.save(location);
    }

    // ---- Stock / Inventory ----

    public List<StockItem> findAvailableStock(String tenantId, String productId) {
        return stockItemRepository.findAvailableByProductFEFO(tenantId, productId);
    }

    public Integer getTotalAvailable(String tenantId, String productId) {
        Integer qty = stockItemRepository.getTotalAvailableQty(tenantId, productId);
        return qty != null ? qty : 0;
    }

    /**
     * Get inventory summary: productId -> available quantity
     */
    public Map<String, Integer> getInventorySummary(String tenantId, String locationId) {
        return stockItemRepository.getStockSummaryByLocation(tenantId, locationId)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> row[1] != null ? ((Number) row[1]).intValue() : 0
                ));
    }

    /**
     * Stock entry - receive items from purchase order
     */
    @Transactional
    public StockItem receiveStock(StockItem stockItem) {
        stockItem.setStatus(StockItem.ItemStatus.AVAILABLE);
        return stockItemRepository.save(stockItem);
    }

    /**
     * Dispense stock using FEFO (First Expired, First Out)
     */
    @Transactional
    public void dispenseStock(String tenantId, String productId, int quantity, String admissionId) {
        List<StockItem> available = stockItemRepository.findAvailableByProductFEFO(tenantId, productId);
        int remaining = quantity;
        for (StockItem item : available) {
            if (remaining <= 0) break;
            int itemQty = item.getQuantityUnits() != null ? item.getQuantityUnits() : 0;
            if (itemQty <= remaining) {
                item.setStatus(StockItem.ItemStatus.DISPENSED);
                remaining -= itemQty;
            } else {
                item.setQuantityUnits(itemQty - remaining);
                remaining = 0;
            }
            stockItemRepository.save(item);
        }
        if (remaining > 0) {
            throw new IllegalStateException("Stock insuffisant pour le produit: " + productId);
        }
    }

    /**
     * Put stock in quarantine
     */
    @Transactional
    public void quarantineStock(String itemId, String reason) {
        stockItemRepository.findById(itemId).ifPresent(item -> {
            item.setStatus(StockItem.ItemStatus.QUARANTINE);
            stockItemRepository.save(item);
        });
    }
}
