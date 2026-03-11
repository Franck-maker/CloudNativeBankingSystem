package com.bank.banking_system.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bank.grpc.notification.NotificationServiceGrpc;
import com.bank.grpc.notification.TransferAlertRequest;
import com.bank.grpc.notification.TransferAlertResponse;

import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * This class is a placeholder for the gRPC client that will communicate with the Notification Service.
 * It will be responsible for sending notification requests to the Notification Service when certain events occur
 * it'll use the stubs methods generated from the .proto file to make RPC calls to the Notification Service.
 */

@Service
public class NotificationGrpcClient {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClient.class);

    // This annotation tells Spring Boot to inject the network client pointing to our Node.js server
    @GrpcClient("notification-service")
    private NotificationServiceGrpc.NotificationServiceBlockingStub notificationStub;

    public void sendTransferAlert(String accountId, double amount, String message) {
        log.info("Attempting to send gRPC alert to Node.js for account: {}", accountId);

        try {
            // 1. Build the Protobuf Request using the auto-generated Builder
            TransferAlertRequest request = TransferAlertRequest.newBuilder()
                    .setAccountId(accountId)
                    .setAmount(amount)
                    .setMessage(message)
                    .build();

            // 2. Fire the binary RPC call over the network
            TransferAlertResponse response = notificationStub.sendTransferAlert(request);

            // 3. Log the response from Node.js
            log.info("gRPC Response Received: Success={}, Timestamp={}, Details={}", 
                     response.getSuccess(), response.getTimestamp(), response.getDetails());

        } catch (Exception e) {
            log.error("Failed to send gRPC alert: {}", e.getMessage());
        }
    }
}
