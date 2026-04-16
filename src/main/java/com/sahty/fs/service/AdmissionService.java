package com.sahty.fs.service;

import com.sahty.fs.entity.*;
import com.sahty.fs.repository.*;
import com.sahty.fs.repository.AdmissionRepository;
import com.sahty.fs.repository.PatientRepository;
import com.sahty.fs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;
    private final ClinicalExamRepository clinicalExamRepository;
    private final ObservationRepository observationRepository;
    private final TransfusionRepository transfusionRepository;
    private final InterventionRepository interventionRepository;

    public List<Admission> findActiveAdmissions(String tenantId) {
        return admissionRepository.findActiveByTenant(tenantId);
    }

    public Page<Admission> findAdmissions(String tenantId, Admission.AdmissionStatus status, int page, int size) {
        return admissionRepository.findByTenantAndStatus(tenantId, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "admissionDate")));
    }

    public Optional<Admission> findById(String id) {
        return admissionRepository.findById(id);
    }

    public List<Admission> findByPatient(String patientId) {
        return admissionRepository.findByPatientIdOrderByAdmissionDateDesc(patientId);
    }

    @Transactional
    public Admission createAdmission(Admission admission, String patientId, String tenantId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient non trouvé"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant non trouvé"));

        admission.setPatient(patient);
        admission.setTenant(tenant);
        admission.setStatus(Admission.AdmissionStatus.EN_COURS);
        admission.setNda(generateNda(tenantId));
        if (admission.getAdmissionDate() == null) {
            admission.setAdmissionDate(LocalDateTime.now());
        }
        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission updateAdmission(Admission admission) {
        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission closeAdmission(String id, LocalDateTime dischargeDate) {
        Admission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admission non trouvée"));
        admission.setStatus(Admission.AdmissionStatus.SORTI);
        admission.setDischargeDate(dischargeDate != null ? dischargeDate : LocalDateTime.now());
        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission cancelAdmission(String id) {
        Admission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admission non trouvée"));
        admission.setStatus(Admission.AdmissionStatus.ANNULE);
        return admissionRepository.save(admission);
    }

    @Transactional
    public ClinicalExam saveClinicalExam(ClinicalExam exam) {
        return clinicalExamRepository.save(exam);
    }

    @Transactional
    public Observation saveObservation(Observation observation) {
        return observationRepository.save(observation);
    }

    @Transactional
    public Transfusion saveTransfusion(Transfusion transfusion) {
        return transfusionRepository.save(transfusion);
    }

    @Transactional
    public Intervention saveIntervention(Intervention intervention) {
        return interventionRepository.save(intervention);
    }

    public long countActiveAdmissions(String tenantId) {
        return admissionRepository.countByTenantIdAndStatus(tenantId, Admission.AdmissionStatus.EN_COURS);
    }

    private String generateNda(String tenantId) {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = admissionRepository.countByTenantIdAndStatus(tenantId, Admission.AdmissionStatus.EN_COURS);
        return "NDA-" + year + "-" + String.format("%06d", count + 1);
    }
}
