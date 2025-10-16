package org.example.client;

import org.example.ddossimulation.AttackRequest;
import org.example.ddossimulation.AttackResponse;

public interface AttackClient {
    AttackResponse sendAttack(AttackRequest request);
}
