package com.tajelectric.electrohub.repository;

import com.tajelectric.electrohub.entity.ServiceComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceComplaintRepository extends JpaRepository<ServiceComplaint, Long> {
    List<ServiceComplaint> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ServiceComplaint> findAllByOrderByCreatedAtDesc();
    long countByComplaintNoStartingWith(String prefix);
}
