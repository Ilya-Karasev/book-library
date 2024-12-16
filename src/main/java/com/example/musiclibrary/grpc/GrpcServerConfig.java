package com.example.musiclibrary.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class GrpcServerConfig {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private BookServiceImpl bookService;
    @Autowired
    private RentalServiceImpl rentalService;
    @Autowired
    private ReservationServiceImpl reservationService;
    @Bean
    public Server grpcServer() throws IOException {
        return ServerBuilder
                .forPort(9090)
                .addService(userService)
                .addService(bookService)
                .addService(rentalService)
                .addService(reservationService)
                .build()
                .start();
    }
}