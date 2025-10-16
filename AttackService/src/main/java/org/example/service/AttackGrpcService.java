package org.example.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.ddossimulation.*;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AttackGrpcService extends AttackServiceGrpc.AttackServiceImplBase {

    @GrpcClient("defenseService")
    private DefenseServiceGrpc.DefenseServiceBlockingStub defenseStub;

    private volatile boolean attacking = false;
    private ScheduledExecutorService executor;
    private final Random random = new Random();
    private final AtomicInteger requestId = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);

    @Override
    public void startAttack(StartRequest request, StreamObserver<StartResponse> responseObserver) {
        startAttackInternal();
        responseObserver.onNext(StartResponse.newBuilder().setStarted(true).build());
        responseObserver.onCompleted();
    }

    public void startAttackInternal() {
        if (attacking) return;
        attacking = true;
        executor = Executors.newScheduledThreadPool(10);

        executor.scheduleAtFixedRate(() -> {
            int rps = random.nextInt(1000);
            for (int i = 0; i < rps; i++) {
                int id = requestId.incrementAndGet();
                sendAttack(id);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void sendAttack(int id) {
        try {
            AttackRequest request = AttackRequest.newBuilder()
                    .setId(id)
                    .setType("TCP")
                    .build();

            AttackResponse response = defenseStub.handleAttack(request);

            if (response.getSuccess()) {
                successCount.incrementAndGet();
                System.out.println("✅ Attack " + id + ": " + response.getMessage());
            } else {
                failCount.incrementAndGet();
                System.out.println("❌ Attack " + id + ": " + response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                System.out.println("🚫 503 ERROR - Attack " + id + ": " + e.getStatus().getDescription());
            } else {
                System.out.println("⚠️ gRPC Error - Attack " + id + ": " + e.getMessage());
            }
            failCount.incrementAndGet();
        } catch (Exception e) {
            failCount.incrementAndGet();
            System.out.println("❌ Unexpected error - Attack " + id + ": " + e.getMessage());
        }
    }

    public void stopAttack() {
        if (!attacking) return;
        attacking = false;
        if (executor != null) executor.shutdownNow();

        try {
            defenseStub.stopAttack(
                    StopRequest.newBuilder().setReason("Manual stop").build()
            );
        } catch (Exception e) {
            System.out.println("⚠️ Could not notify defense service: " + e.getMessage());
        }
    }

    public boolean isAttacking() {
        return attacking;
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailCount() {
        return failCount.get();
    }
}