package com.tajelectric.electrohub.config;

import com.tajelectric.electrohub.entity.Technician;
import com.tajelectric.electrohub.repository.TechnicianRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class TechnicianSeeder implements CommandLineRunner {

    private final TechnicianRepository techRepo;

    public TechnicianSeeder(TechnicianRepository techRepo) {
        this.techRepo = techRepo;
    }

    @Override
    public void run(String... args) {
        if (techRepo.count() > 0) return;
        String[][] rows = {
                {"Rahul Sharma", "9876543210", "AC & Refrigerator Expert"},
                {"Mohd. Imran", "9123456789", "Wiring & Heavy Electricals"},
                {"Suresh Kumar", "9988776655", "Washing Machine & Geyser"},
                {"Amit Verma", "9811223344", "General Appliances & Fans"},
        };
        for (String[] r : rows) {
            Technician t = new Technician();
            t.setName(r[0]);
            t.setPhone(r[1]);
            t.setSpecialization(r[2]);
            t.setStatus("Available");
            t.setActiveJobs(0);
            techRepo.save(t);
        }
    }
}
