package com.tajelectric.electrohub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "technicians")
public class Technician {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String phone;
    private String specialization = "General Appliances";
    @Column(nullable = false)
    private String status = "Available"; // Available | On Field
    @Column(name = "active_jobs")
    private Integer activeJobs = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getActiveJobs() { return activeJobs; }
    public void setActiveJobs(Integer activeJobs) { this.activeJobs = activeJobs; }
}
