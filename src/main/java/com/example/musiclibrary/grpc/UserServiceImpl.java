package com.example.musiclibrary.grpc;

import com.example.musiclibrary.*;
import com.example.musiclibrary.models.User;
import com.example.musiclibrary.repositories.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("userServiceImplGrpc")
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void findUser(UserNameRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findByName(request.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            UserResponse response = UserResponse.newBuilder()
                    .setName(user.getName() != null ? user.getName() : "null")
                    .setEmail(user.getEmail() != null ? user.getEmail() : "null")
                    .setPhoneNumber(user.getPhone_number() != null ? user.getPhone_number() : "null")
                    .setAddress(user.getAddress() != null ? user.getAddress() : "null")
                    .setRole(user.getRole().name() != null ? user.getRole().name() : "null")
                    .build();

            responseObserver.onNext(response);
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Пользователь с именем " + request.getName() + " не найден")
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAllUsers(EmptyRequest request, StreamObserver<UserListResponse> responseObserver) {
        List<User> users = userRepository.findAll();

        List<UserResponse> userResponses = users.stream()
                .map(user -> UserResponse.newBuilder()
                        .setName(user.getName() != null ? user.getName() : "null")
                        .setEmail(user.getEmail() != null ? user.getEmail() : "null")
                        .setPhoneNumber(user.getPhone_number() != null ? user.getPhone_number() : "null")
                        .setAddress(user.getAddress() != null ? user.getAddress() : "null")
                        .setRole(user.getRole().name() != null ? user.getRole().name() : "null")
                        .build())
                .collect(Collectors.toList());

        UserListResponse response = UserListResponse.newBuilder()
                .addAllUsers(userResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}