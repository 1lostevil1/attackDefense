package org.example.client;

import org.example.ddossimulation.AttackRequest;
import org.example.ddossimulation.AttackResponse;
import org.example.message.AttackMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.mode", havingValue = "rabbit")
public class RabbitAttackClient implements AttackClient {

    private final RabbitTemplate rabbitTemplate;

    public RabbitAttackClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public AttackResponse sendAttack(AttackRequest request) {
        AttackMessage msg = new AttackMessage(request.getId());
        Object reply = rabbitTemplate.convertSendAndReceive("attack.exchange", "attack.routingkey", msg);

        if (reply instanceof AttackResponse) {
            return (AttackResponse) reply;
        } else {
            return AttackResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("No response or timeout")
                    .build();
        }
    }
}
