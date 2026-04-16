package com.sahty.fs.service.lims;

import com.sahty.fs.entity.lims.*;
import com.sahty.fs.repository.lims.LabAnalyteRepository;
import com.sahty.fs.repository.lims.LabSampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LimsService {

    private final LabSampleRepository sampleRepository;
    private final LabAnalyteRepository analyteRepository;

    public List<LabSample> findSamplesByPatient(String patientId) {
        return sampleRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<LabSample> findSamplesByAdmission(String admissionId) {
        return sampleRepository.findByAdmissionIdOrderByCreatedAtDesc(admissionId);
    }

    public Optional<LabSample> findSampleById(String id) {
        return sampleRepository.findById(id);
    }

    public List<LabSample> findPendingCollections(String tenantId) {
        return sampleRepository.findByTenantIdAndStatus(tenantId, LabSample.SampleStatus.REGISTERED);
    }

    public List<LabSample> findPendingReceptions(String tenantId) {
        return sampleRepository.findByTenantIdAndStatus(tenantId, LabSample.SampleStatus.COLLECTED);
    }

    @Transactional
    public LabSample registerSample(LabSample sample, String tenantId) {
        sample.setTenantId(tenantId);
        sample.setStatus(LabSample.SampleStatus.REGISTERED);
        sample.setSampleNumber(generateSampleNumber(tenantId));
        return sampleRepository.save(sample);
    }

    @Transactional
    public LabSample collectSample(String sampleId) {
        LabSample sample = getOrThrow(sampleId);
        sample.setStatus(LabSample.SampleStatus.COLLECTED);
        sample.setCollectionDateTime(LocalDateTime.now());
        return sampleRepository.save(sample);
    }

    @Transactional
    public LabSample receiveSample(String sampleId) {
        LabSample sample = getOrThrow(sampleId);
        sample.setStatus(LabSample.SampleStatus.RECEIVED);
        sample.setReceptionDateTime(LocalDateTime.now());
        return sampleRepository.save(sample);
    }

    @Transactional
    public LabSample rejectSample(String sampleId, String reason) {
        LabSample sample = getOrThrow(sampleId);
        sample.setStatus(LabSample.SampleStatus.REJECTED);
        sample.setNotes(reason);
        return sampleRepository.save(sample);
    }

    public List<LabAnalyte> findAllAnalytes() {
        return analyteRepository.findByIsActiveTrue();
    }

    public List<LabAnalyte> findAnalytesBySection(String sectionId) {
        return analyteRepository.findBySectionIdAndIsActiveTrue(sectionId);
    }

    @Transactional
    public LabAnalyte saveAnalyte(LabAnalyte analyte) {
        return analyteRepository.save(analyte);
    }

    private LabSample getOrThrow(String id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Échantillon non trouvé: " + id));
    }

    private String generateSampleNumber(String tenantId) {
        String prefix = tenantId.substring(0, Math.min(3, tenantId.length())).toUpperCase();
        return prefix + "-" + System.currentTimeMillis();
    }
}
