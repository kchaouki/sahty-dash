package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabSampleRepository extends JpaRepository<LabSample, String> {
    Optional<LabSample> findBySampleNumber(String sampleNumber);
    List<LabSample> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<LabSample> findByAdmissionIdOrderByCreatedAtDesc(String admissionId);
    List<LabSample> findByTenantIdAndStatus(String tenantId, LabSample.SampleStatus status);
}
