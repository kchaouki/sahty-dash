package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabAnalyte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabAnalyteRepository extends JpaRepository<LabAnalyte, String> {
    List<LabAnalyte> findByIsActiveTrue();
    List<LabAnalyte> findBySectionIdAndIsActiveTrue(String sectionId);
    List<LabAnalyte> findBySubSectionIdAndIsActiveTrue(String subSectionId);
}
