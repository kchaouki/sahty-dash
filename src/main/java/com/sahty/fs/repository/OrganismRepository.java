package com.sahty.fs.repository;

import com.sahty.fs.entity.Organism;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganismRepository extends JpaRepository<Organism, String> {
    List<Organism> findByIsActiveTrue();
    List<Organism> findByType(String type);
}
