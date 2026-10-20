package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.ServiceComplaint;
import com.tajelectric.electrohub.entity.Technician;
import com.tajelectric.electrohub.repository.ServiceComplaintRepository;
import com.tajelectric.electrohub.repository.TechnicianRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ComplaintController {

    private static final double VISIT = 199;

    private final ServiceComplaintRepository complaintRepo;
    private final TechnicianRepository techRepo;

    public ComplaintController(ServiceComplaintRepository complaintRepo, TechnicianRepository techRepo) {
        this.complaintRepo = complaintRepo;
        this.techRepo = techRepo;
    }

    private String nextComplaintNo() {
        String prefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long n = complaintRepo.countByComplaintNoStartingWith(prefix) + 1;
        return prefix + String.format("%02d", n);
    }

    @PostMapping("/api/complaints")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> b) {
        String name = str(b.get("customerName"));
        String phone = str(b.get("phone")).replaceAll("\\D", "");
        String address = str(b.get("address"));
        if (name.isBlank() || phone.length() < 10 || address.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name, 10-digit phone and address are required."));
        }
        ServiceComplaint c = new ServiceComplaint();
        c.setComplaintNo(nextComplaintNo());
        c.setUserId(AuthUtil.currentUserId());
        c.setCustomerName(name);
        c.setPhone(phone.length() > 10 ? phone.substring(phone.length() - 10) : phone);
        c.setAltPhone(str(b.get("altPhone")));
        c.setAddress(address);
        c.setLandmark(str(b.get("landmark")));
        c.setServiceCategory(str(b.getOrDefault("serviceCategory", "home_wiring")));
        c.setServiceName(str(b.getOrDefault("serviceName", "Home Wiring & Repairing")));
        Object probs = b.get("selectedProblems");
        if (probs instanceof List<?> list && !list.isEmpty()) {
            c.setSelectedProblems(list.stream().map(Object::toString).collect(Collectors.toList()));
        } else {
            c.setSelectedProblems(List.of("General Inspection / Fault Diagnosis"));
        }
        c.setNotes(str(b.get("notes")));
        c.setVisitCharge(VISIT);
        c.setRepairCharge(0.0);
        c.setTotalCharge(VISIT);
        c.setStatus("Pending");
        c.setPaymentStatus("Pending");
        return ResponseEntity.ok(Map.of("complaint", complaintRepo.save(c)));
    }

    @GetMapping("/api/complaints/mine")
    public Map<String, Object> mine() {
        Long uid = AuthUtil.currentUserId();
        if (uid == null) return Map.of("complaints", List.of());
        return Map.of("complaints", complaintRepo.findByUserIdOrderByCreatedAtDesc(uid));
    }

    @GetMapping("/api/admin/complaints")
    public Map<String, Object> all() {
        return Map.of("complaints", complaintRepo.findAllByOrderByCreatedAtDesc());
    }

    @PatchMapping("/api/admin/complaints/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        ServiceComplaint c = complaintRepo.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

        if (b.get("techId") != null && !b.get("techId").toString().isBlank()) {
            Long techId = Long.valueOf(b.get("techId").toString());
            Technician t = techRepo.findById(techId).orElse(null);
            if (t == null) return ResponseEntity.badRequest().body(Map.of("error", "Technician not found"));
            // un-assign previous
            if (c.getAssignedTechId() != null && !c.getAssignedTechId().equals(techId)) {
                techRepo.findById(c.getAssignedTechId()).ifPresent(old -> {
                    old.setActiveJobs(Math.max(0, (old.getActiveJobs() == null ? 0 : old.getActiveJobs()) - 1));
                    if (old.getActiveJobs() == 0) old.setStatus("Available");
                    techRepo.save(old);
                });
            }
            if (c.getAssignedTechId() == null || !c.getAssignedTechId().equals(techId)) {
                t.setActiveJobs((t.getActiveJobs() == null ? 0 : t.getActiveJobs()) + 1);
                t.setStatus("On Field");
                techRepo.save(t);
            }
            c.setAssignedTechId(t.getId());
            c.setAssignedTechName(t.getName());
            c.setAssignedTechPhone(t.getPhone());
            if ("Pending".equals(c.getStatus())) c.setStatus("Technician Assigned");
        }

        if (b.get("status") != null) {
            String st = b.get("status").toString();
            c.setStatus(st);
            if ("Completed".equals(st)) {
                c.setPaymentStatus("Paid");
                if (c.getAssignedTechId() != null) {
                    techRepo.findById(c.getAssignedTechId()).ifPresent(t -> {
                        t.setActiveJobs(Math.max(0, (t.getActiveJobs() == null ? 0 : t.getActiveJobs()) - 1));
                        if (t.getActiveJobs() == 0) t.setStatus("Available");
                        techRepo.save(t);
                    });
                }
            }
        }
        if (b.get("repairCharge") != null) {
            double r = Double.parseDouble(b.get("repairCharge").toString());
            c.setRepairCharge(r);
            c.setTotalCharge((c.getVisitCharge() == null ? VISIT : c.getVisitCharge()) + r);
        }
        return ResponseEntity.ok(Map.of("complaint", complaintRepo.save(c)));
    }

    @GetMapping("/api/technicians")
    public Map<String, Object> publicTechs() {
        // customers only see availability, not full roster phones optional
        return Map.of("technicians", techRepo.findAll());
    }

    @GetMapping("/api/admin/technicians")
    public Map<String, Object> techs() {
        return Map.of("technicians", techRepo.findAll());
    }

    @PostMapping("/api/admin/technicians")
    public Map<String, Object> addTech(@RequestBody Map<String, Object> b) {
        Technician t = new Technician();
        t.setName(str(b.get("name")));
        t.setPhone(str(b.get("phone")).replaceAll("\\D", ""));
        t.setSpecialization(str(b.getOrDefault("specialization", "General Appliances")));
        t.setStatus("Available");
        t.setActiveJobs(0);
        return Map.of("technician", techRepo.save(t));
    }

    @DeleteMapping("/api/admin/technicians/{id}")
    public Map<String, Object> delTech(@PathVariable Long id) {
        techRepo.deleteById(id);
        return Map.of("ok", true);
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }
}
