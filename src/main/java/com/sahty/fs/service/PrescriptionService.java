package com.sahty.fs.service;

import com.sahty.fs.entity.Admission;
import com.sahty.fs.entity.Prescription;
import com.sahty.fs.repository.AdmissionRepository;
import com.sahty.fs.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AdmissionRepository admissionRepository;

    public List<Prescription> findByAdmission(String admissionId) {
        return prescriptionRepository.findByAdmissionId(admissionId);
    }

    public List<Prescription> findActiveByAdmission(String admissionId) {
        return prescriptionRepository.findByAdmissionIdAndStatus(admissionId, Prescription.PrescriptionStatus.ACTIVE);
    }

    public Optional<Prescription> findById(String id) {
        return prescriptionRepository.findById(id);
    }

    @Transactional
    public Prescription create(Prescription prescription, String admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new IllegalArgumentException("Admission non trouvée"));
        prescription.setAdmission(admission);
        prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        if (prescription.getStartDateTime() == null) {
            prescription.setStartDateTime(LocalDateTime.now());
        }
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription save(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription update(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription pause(String id) {
        Prescription p = getOrThrow(id);
        p.setStatus(Prescription.PrescriptionStatus.PAUSED);
        p.setPausedAt(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    @Transactional
    public Prescription resume(String id) {
        Prescription p = getOrThrow(id);
        p.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        p.setPausedAt(null);
        return prescriptionRepository.save(p);
    }

    @Transactional
    public Prescription stop(String id) {
        Prescription p = getOrThrow(id);
        p.setStatus(Prescription.PrescriptionStatus.STOPPED);
        p.setStoppedAt(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    @Transactional
    public void delete(String id) {
        prescriptionRepository.deleteById(id);
    }

    private Prescription getOrThrow(String id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prescription non trouvée: " + id));
    }
}
