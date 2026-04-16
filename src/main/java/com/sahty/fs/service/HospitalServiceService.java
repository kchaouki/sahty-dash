package com.sahty.fs.service;

import com.sahty.fs.entity.HospitalService;
import com.sahty.fs.repository.HospitalServiceRepository;
import com.sahty.fs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalServiceService {
    private final HospitalServiceRepository serviceRepository;
    private final TenantRepository tenantRepository;

    public List<HospitalService> findByTenant(String tenantId) {
        return serviceRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    public Optional<HospitalService> findById(String id) {
        return serviceRepository.findById(id);
    }

    @Transactional
    public HospitalService save(HospitalService service, String tenantId) {
        if (service.getTenant() == null) {
            tenantRepository.findById(tenantId).ifPresent(service::setTenant);
        }
        return serviceRepository.save(service);
    }
}
