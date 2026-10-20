package com.tajelectric.electrohub.repository;

import com.tajelectric.electrohub.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    List<Technician> findByStatus(String status);
}
