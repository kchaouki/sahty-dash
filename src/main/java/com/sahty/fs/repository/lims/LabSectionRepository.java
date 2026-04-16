package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabSectionRepository extends JpaRepository<LabSection, String> {
    List<LabSection> findByIsActiveTrue();
}
