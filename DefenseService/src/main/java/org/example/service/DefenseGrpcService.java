package org.example.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.ddossimulation.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@GrpcService
public class DefenseGrpcService extends DefenseServiceGrpc.DefenseServiceImplBase {

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicInteger periodRequestCount = new AtomicInteger(0);

    private final AtomicInteger blockedCount = new AtomicInteger(0);
    private final AtomicInteger error503Count = new AtomicInteger(0);

    private final AtomicInteger speed = new AtomicInteger(0);

    @Override
    public void handleAttack(AttackRequest request, StreamObserver<AttackResponse> responseObserver) {
        requestCount.incrementAndGet();

        if (speed.get()>200) {
            error503Count.incrementAndGet();
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription("Service temporarily unavailable - banned")
                    .asRuntimeException());
            return;
        }

        periodRequestCount.incrementAndGet();
        AttackResponse response = AttackResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Processed")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void stopAttack(StopRequest request, StreamObserver<StopResponse> responseObserver) {

        StopResponse response = StopResponse.newBuilder()
                .setStopped(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public int getBlockedCount() {
        return blockedCount.get();
    }

    public int getError503Count() {
        return error503Count.get();
    }

    public void resetCounters() {
        requestCount.set(0);
        blockedCount.set(0);
        error503Count.set(0);
    }

    @Scheduled(fixedRate = 10000)
    private void job(){
        System.out.println("job running");
        speed.set(periodRequestCount.get()/10);
        System.out.println("speed: "+speed.get());
        periodRequestCount.set(0);
    }
}