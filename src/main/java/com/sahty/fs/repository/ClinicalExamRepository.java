package com.sahty.fs.repository;

import com.sahty.fs.entity.ClinicalExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalExamRepository extends JpaRepository<ClinicalExam, String> {
    List<ClinicalExam> findByAdmissionIdOrderByCreatedAtDesc(String admissionId);
}
