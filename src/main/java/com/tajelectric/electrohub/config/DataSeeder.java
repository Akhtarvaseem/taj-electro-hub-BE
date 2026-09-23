package com.tajelectric.electrohub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CatalogSeeder catalogSeeder;

    public DataSeeder(CatalogSeeder catalogSeeder) {
        this.catalogSeeder = catalogSeeder;
    }

    @Override
    public void run(String... args) {
        catalogSeeder.seed(false);
    }
}
