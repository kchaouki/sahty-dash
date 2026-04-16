package com.sahty.fs.repository;

import com.sahty.fs.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
    List<Prescription> findByAdmissionId(String admissionId);
    List<Prescription> findByAdmissionIdAndStatus(String admissionId, Prescription.PrescriptionStatus status);
    List<Prescription> findByAdmissionIdAndType(String admissionId, Prescription.PrescriptionType type);
}
