package org.example.service;

import org.example.ddossimulation.AttackResponse;
import org.example.message.AttackMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DefenseRabbitListener {

    private final Random random = new Random();

    @RabbitListener(queues = "attack.queue")
    public AttackResponse handleAttack(AttackMessage msg) {
        boolean success = random.nextInt(100) > 20; // 20% атак игнорируются
        String message = success ? "Processed id=" + msg.getId() : "Ignored by defense id=" + msg.getId();

        return AttackResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
    }
}