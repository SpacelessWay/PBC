package com.example.pbc.config;

import com.example.pbc.service.Service;
import org.springframework.boot.CommandLineRunner;


public class StartupRunner implements CommandLineRunner {

    private final Service service;

    public StartupRunner(Service service) {
        this.service = service;
    }

    @Override
    public void run(String... args) throws Exception {
        service.init();
    }
}