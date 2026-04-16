package com.sahty.fs.service;

import com.sahty.fs.entity.Allergy;
import com.sahty.fs.entity.Antecedent;
import com.sahty.fs.entity.Patient;
import com.sahty.fs.entity.Tenant;
import com.sahty.fs.repository.AllergyRepository;
import com.sahty.fs.repository.AntecedentRepository;
import com.sahty.fs.repository.PatientRepository;
import com.sahty.fs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;
    private final AllergyRepository allergyRepository;
    private final AntecedentRepository antecedentRepository;

    public Page<Patient> findActivePatients(String tenantId, int page, int size) {
        return patientRepository.findActiveByTenant(tenantId,
                PageRequest.of(page, size, Sort.by("lastName", "firstName")));
    }

    public Page<Patient> searchPatients(String tenantId, String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return findActivePatients(tenantId, page, size);
        }
        return patientRepository.searchByTenant(tenantId, query,
                PageRequest.of(page, size));
    }

    public Optional<Patient> findById(String id) {
        return patientRepository.findById(id);
    }

    @Transactional
    public Patient createPatient(Patient patient, String tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant non trouvé"));
        patient.setTenant(tenant);
        patient.setTenantPatientId(UUID.randomUUID().toString());
        patient.setLifecycleStatus(Patient.LifecycleStatus.ACTIVE);
        if (patient.getIpp() == null || patient.getIpp().isBlank()) {
            patient.setIpp(generateIpp(tenantId));
        }
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updatePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Transactional
    public void deactivatePatient(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient non trouvé"));
        patient.setLifecycleStatus(Patient.LifecycleStatus.INACTIVE);
        patientRepository.save(patient);
    }

    @Transactional
    public Allergy saveAllergy(Allergy allergy) {
        return allergyRepository.save(allergy);
    }

    @Transactional
    public Antecedent saveAntecedent(Antecedent antecedent) {
        return antecedentRepository.save(antecedent);
    }

    public long countActivePatients(String tenantId) {
        return patientRepository.countByTenantIdAndLifecycleStatus(tenantId, Patient.LifecycleStatus.ACTIVE);
    }

    private String generateIpp(String tenantId) {
        long count = patientRepository.countByTenantIdAndLifecycleStatus(tenantId, Patient.LifecycleStatus.ACTIVE);
        return String.format("IPP-%06d", count + 1);
    }
}
