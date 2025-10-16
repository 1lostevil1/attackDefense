package org.example.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.ddossimulation.AttackRequest;
import org.example.ddossimulation.AttackResponse;
import org.example.ddossimulation.DefenseServiceGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.mode", havingValue = "grpc", matchIfMissing = true)
public class GrpcAttackClient implements AttackClient {

    @GrpcClient("defenseService")
    private DefenseServiceGrpc.DefenseServiceBlockingStub defenseClient;

    @Override
    public AttackResponse sendAttack(AttackRequest request) {
        return defenseClient.handleAttack(request);
    }
}
