package org.example.controller;

import org.example.service.AttackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
public class AttackController {

    @Autowired
    private AttackService attackService;

    @PostMapping("/start")
    public String startAttack() {
        attackService.startAttackInternal();
        return String.format("Attack started");
    }

    @PostMapping("/stop")
    public String stopAttack() {
        attackService.stopAttack();
        return "Attack stopped";
    }

    @GetMapping("/stats")
    public String getStats() {
        return String.format("Success: %d, Failed: %d, Attacking: %s",
                attackService.getSuccessCount(),
                attackService.getFailCount(),
                attackService.isAttacking());
    }

}
