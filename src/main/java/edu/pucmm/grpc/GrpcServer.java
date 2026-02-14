package edu.pucmm.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);
    private Server server;

    private void start() throws IOException {
        int port = 50051; // Ajusta el puerto si prefieres otro
        server = ServerBuilder.forPort(port)
                .addService(new UrlExtendedServiceImpl())
                .build()
                .start();

        LOGGER.info("gRPC Server started, listening on {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down gRPC server since JVM is shutting down");
            GrpcServer.this.stop();
            LOGGER.info("gRPC server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    // Bloquea el hilo principal para mantener el servidor en ejecución
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final GrpcServer grpcServer = new GrpcServer();
        grpcServer.start();
        grpcServer.blockUntilShutdown();
    }
}
