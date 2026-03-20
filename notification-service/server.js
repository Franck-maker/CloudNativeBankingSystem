const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const path = require('path');

// ==========================================
// 1. Load the Protobuf Contract
// ==========================================
const PROTO_PATH = __dirname + '/notification.proto';

// This parses the .proto file dynamically at runtime
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

// Extract the 'notification' package defined in the .proto file
const notificationProto = grpc.loadPackageDefinition(packageDefinition).notification;

// ==========================================
// 2. Implement the Service Logic
// ==========================================
// This function name matches the rpc definition in the .proto file (camelCase)
function sendTransferAlert(call, callback) {
    const request = call.request;
    
    console.log('\n----------------------------------------');
    console.log(`[gRPC EVENT] 🚨 Transfer Alert Received!`);
    console.log(`[gRPC EVENT] 🏦 Account ID: ${request.account_id}`);
    console.log(`[gRPC EVENT] 💰 Amount: $${request.amount}`);
    console.log(`[gRPC EVENT] ✉️  Message: "${request.message}"`);
    console.log('----------------------------------------\n');

    // Here is where we would normally connect to an email API like SendGrid or AWS SES.
    // For this project, we just log it to prove the microservices are talking.
    console.log(`[gRPC EVENT] Processing SMS/Email notification... DONE.`);

    // Send the response back to the Spring Boot client
    callback(null, {
        success: true,
        timestamp: new Date().toISOString(),
        details: "Notification successfully processed by Node.js Microservice"
    });
}

// ==========================================
// 3. Boot up the gRPC Server
// ==========================================
function main() {
    const server = new grpc.Server();
    
    // Bind our implementation to the NotificationService contract
    server.addService(notificationProto.NotificationService.service, {
        sendTransferAlert: sendTransferAlert
    });

    // Port 50051 is the universal standard port for gRPC servers
    const PORT = '0.0.0.0:50051';
    
    server.bindAsync(PORT, grpc.ServerCredentials.createInsecure(), (error, port) => {
        if (error) {
            console.error(`[FATAL] Failed to bind server: ${error.message}`);
            return;
        }
        console.log(`🚀 Node.js gRPC Notification Service is listening on ${PORT}`);
    });
}

main();