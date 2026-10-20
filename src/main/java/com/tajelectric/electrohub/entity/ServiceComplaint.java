package com.tajelectric.electrohub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_complaints")
public class ServiceComplaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_no", nullable = false, unique = true)
    private String complaintNo;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;
    @Column(nullable = false)
    private String phone;
    @Column(name = "alt_phone")
    private String altPhone;
    @Column(nullable = false, columnDefinition = "text")
    private String address;
    private String landmark;

    @Column(name = "service_category")
    private String serviceCategory;
    @Column(name = "service_name")
    private String serviceName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "complaint_problems", joinColumns = @JoinColumn(name = "complaint_id"))
    @Column(name = "problem", length = 400)
    private List<String> selectedProblems = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "visit_charge")
    private Double visitCharge = 199.0;
    @Column(name = "repair_charge")
    private Double repairCharge = 0.0;
    @Column(name = "total_charge")
    private Double totalCharge = 199.0;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(name = "assigned_tech_id")
    private Long assignedTechId;
    @Column(name = "assigned_tech_name")
    private String assignedTechName;
    @Column(name = "assigned_tech_phone")
    private String assignedTechPhone;

    @Column(name = "payment_status")
    private String paymentStatus = "Pending";

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getComplaintNo() { return complaintNo; }
    public void setComplaintNo(String complaintNo) { this.complaintNo = complaintNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAltPhone() { return altPhone; }
    public void setAltPhone(String altPhone) { this.altPhone = altPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public List<String> getSelectedProblems() { return selectedProblems; }
    public void setSelectedProblems(List<String> selectedProblems) { this.selectedProblems = selectedProblems; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Double getVisitCharge() { return visitCharge; }
    public void setVisitCharge(Double visitCharge) { this.visitCharge = visitCharge; }
    public Double getRepairCharge() { return repairCharge; }
    public void setRepairCharge(Double repairCharge) { this.repairCharge = repairCharge; }
    public Double getTotalCharge() { return totalCharge; }
    public void setTotalCharge(Double totalCharge) { this.totalCharge = totalCharge; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAssignedTechId() { return assignedTechId; }
    public void setAssignedTechId(Long assignedTechId) { this.assignedTechId = assignedTechId; }
    public String getAssignedTechName() { return assignedTechName; }
    public void setAssignedTechName(String assignedTechName) { this.assignedTechName = assignedTechName; }
    public String getAssignedTechPhone() { return assignedTechPhone; }
    public void setAssignedTechPhone(String assignedTechPhone) { this.assignedTechPhone = assignedTechPhone; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
