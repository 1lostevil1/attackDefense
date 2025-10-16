package org.example.controller;

import org.example.service.DefenseGrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefenseController {

    @Autowired
    private DefenseGrpcService defenseService;

    @GetMapping("/status")
    public String getStats() {
        return String.format("Requests: %d, Blocked: %d, 503 Errors: %d",
                defenseService.getRequestCount(),
                defenseService.getBlockedCount(),
                defenseService.getError503Count());
    }

    @PostMapping("/reset")
    public String reset() {
        defenseService.resetCounters();
        return "All counters and bans reset";
    }

}


