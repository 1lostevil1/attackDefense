package org.example.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.client.AttackClient;
import org.example.ddossimulation.*;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class AttackService extends AttackServiceGrpc.AttackServiceImplBase {

    private final AttackClient attackClient;
    private volatile boolean attacking = false;
    private ScheduledExecutorService executor;
    private Random random = new Random();
    private final AtomicInteger requestId = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);

    public AttackService(AttackClient attackClient) {
        this.attackClient = attackClient;
    }

    @Override
    public void startAttack(StartRequest request, StreamObserver<StartResponse> responseObserver) {
        startAttackInternal();
        responseObserver.onNext(StartResponse.newBuilder().setStarted(true).build());
        responseObserver.onCompleted();
    }

    public void startAttackInternal() {
        if (attacking) return;
        attacking = true;
        executor = Executors.newScheduledThreadPool(15);

        executor.scheduleAtFixedRate(() -> {
            int rps = random.nextInt(1000);
            for (int i = 0; i < rps; i++) sendAttack(requestId.incrementAndGet());
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void sendAttack(int id) {
        try {
            AttackRequest request = AttackRequest.newBuilder().setId(id).build();
            AttackResponse response = attackClient.sendAttack(request);

            if (response.getSuccess()) {
                successCount.incrementAndGet();
                System.out.println("✅ " + id + ": " + response.getMessage());
            } else {
                failCount.incrementAndGet();
                System.out.println("❌ " + id + ": " + response.getMessage());
            }
        } catch (Exception e) {
            failCount.incrementAndGet();
            System.out.println("🚫 " + id + ": " + e.getMessage());
        }
    }

    public void stopAttack() {
        if (!attacking) return;
        attacking = false;
        if (executor != null) executor.shutdown();
    }

    public boolean isAttacking() { return attacking; }
    public int getSuccessCount() { return successCount.get(); }
    public int getFailCount() { return failCount.get(); }
}