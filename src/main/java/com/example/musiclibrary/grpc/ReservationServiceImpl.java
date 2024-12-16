package com.example.musiclibrary.grpc;

import com.example.musiclibrary.*;
import com.example.musiclibrary.models.Book;
import com.example.musiclibrary.models.Reservation;
import com.example.musiclibrary.models.User;
import com.example.musiclibrary.repositories.BookRepository;
import com.example.musiclibrary.repositories.ReservationRepository;
import com.example.musiclibrary.repositories.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("ReservationServiceImplGrpc")
public class ReservationServiceImpl extends ReservationServiceGrpc.ReservationServiceImplBase {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void getReservation(ReservationRequest request, StreamObserver<ReservationResponse> responseObserver) {
        try {
            UUID reservationId = UUID.fromString(request.getId());
            Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
            if (reservationOptional.isPresent()) {
                var reservation = reservationOptional.get();
                ReservationResponse response = ReservationResponse.newBuilder()
                        .setId(reservation.getId() != null ? reservation.getId().toString() : "")
                        .setReservationDate(reservation.getReservation_date() != null ? reservation.getReservation_date().toString() : "")
                        .setExpiryDate(reservation.getExpiry_date() != null ? reservation.getExpiry_date().toString() : "")
                        .setIsActive(Optional.ofNullable(reservation.getIs_active()).orElse(false))
                        .setUser(reservation.getUser().getName())
                        .setBook(reservation.getBook().getTitle())
                        .build();
                responseObserver.onNext(response);
            } else {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Запись бронирования " + request.getId() + " не найдена")
                        .asRuntimeException());
            }
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Некорректный формат id (UUID): " + request.getId())
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAllReservations(EmptyRequest request, StreamObserver<ReservationListResponse> responseObserver) {
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(reserv -> ReservationResponse.newBuilder()
                        .setId(reserv.getId() != null ? reserv.getId().toString() : "")
                        .setReservationDate(reserv.getReservation_date() != null ? reserv.getReservation_date().toString() : "")
                        .setExpiryDate(reserv.getExpiry_date() != null ? reserv.getExpiry_date().toString() : "")
                        .setIsActive(Optional.ofNullable(reserv.getIs_active()).orElse(false))
                        .setUser(reserv.getUser().getName())
                        .setBook(reserv.getBook().getTitle())
                        .build())
                .collect(Collectors.toList());

        ReservationListResponse response = ReservationListResponse.newBuilder()
                .addAllReservations(reservationResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addReservation(ReservationCheckRequest request, StreamObserver<ReservationCheckResponse> responseObserver) {
        try {
            String userName = request.getUser();
            String bookTitle = request.getBook();
            Optional<User> userOpt = userRepository.findByName(userName);
            Optional<Book> bookOpt = bookRepository.findByTitle(bookTitle);

            if (userOpt.isEmpty() || bookOpt.isEmpty()) {
                String errorMessage = userOpt.isEmpty() ? "Пользователь не найден" : "Книга не найдена";
                responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asRuntimeException());
                return;
            }

            User user = userOpt.get();
            Book book = bookOpt.get();

            if (book.getAvailable_copies() == 0) {
                String receipt = generateReceipt(user, book, false, "Нет доступных копий");
                ReservationCheckResponse response = ReservationCheckResponse.newBuilder()
                        .setReceipt(receipt)
                        .setSuccess(false)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            book.setAvailable_copies(book.getAvailable_copies() - 1);
            bookRepository.save(book);

            Reservation reserv = new Reservation();
            reserv.setBook(book);
            reserv.setUser(user);
            reservationRepository.save(reserv);

            String receipt = generateReceipt(user, book, true, "Бронирование успешно оформлено");
            ReservationCheckResponse response = ReservationCheckResponse.newBuilder()
                    .setReceipt(receipt)
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка на сервере").asRuntimeException());
        }
    }

    private String generateReceipt(User user, Book book, boolean success, String message) {
        StringBuilder receiptBuilder = new StringBuilder();
        receiptBuilder.append("==== ЧЕК О БРОНИРОВАНИИ ====\n");
        receiptBuilder.append("Время: ").append(LocalDateTime.now()).append("\n");
        receiptBuilder.append("Пользователь: ").append(user.getName()).append("\n");
        receiptBuilder.append("Книга: ").append(book.getTitle()).append("\n");
        receiptBuilder.append("Статус: ").append(success ? "Успешно" : "Отказано").append("\n");
        receiptBuilder.append("Сообщение: ").append(message).append("\n");
        receiptBuilder.append("================\n");
        return receiptBuilder.toString();
    }
}