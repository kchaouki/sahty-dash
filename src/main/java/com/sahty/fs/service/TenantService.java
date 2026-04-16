package com.sahty.fs.service;

import com.sahty.fs.entity.Tenant;
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
        return tenantRepository.findAllWithGroup();
    }

    public Optional<Tenant> findById(String id) {
        return tenantRepository.findById(id);
    }

    @Transactional
    public Tenant create(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant update(Tenant tenant) {
        return tenantRepository.save(tenant);
    }
}
