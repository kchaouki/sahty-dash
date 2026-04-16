package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabSubSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabSubSectionRepository extends JpaRepository<LabSubSection, String> {
    List<LabSubSection> findByIsActiveTrue();
    List<LabSubSection> findBySectionIdAndIsActiveTrue(String sectionId);
}
