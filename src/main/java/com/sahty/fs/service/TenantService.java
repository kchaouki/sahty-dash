package com.sahty.fs.service;

import com.sahty.fs.entity.Tenant;
import com.sahty.fs.entity.TenantGroup;
import com.sahty.fs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public List<Tenant> findActive() {
        return tenantRepository.findByStatus(Tenant.TenantStatus.ACTIVE);
    }

    public Optional<Tenant> findById(String id) {
        return tenantRepository.findById(id);
    }

    @Transactional
    public Tenant create(Tenant tenant) {
        if (tenantRepository.existsByCode(tenant.getCode())) {
            throw new IllegalArgumentException("Code tenant déjà utilisé: " + tenant.getCode());
        }
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setDbName("tenant_" + tenant.getCode().toLowerCase().replaceAll("[^a-z0-9]", "_"));
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant update(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void suspend(String id) {
        tenantRepository.findById(id).ifPresent(t -> {
            t.setStatus(Tenant.TenantStatus.SUSPENDED);
            tenantRepository.save(t);
        });
    }
}
