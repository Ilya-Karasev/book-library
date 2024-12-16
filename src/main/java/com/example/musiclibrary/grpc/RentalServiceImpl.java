package com.example.musiclibrary.grpc;

import com.example.musiclibrary.*;
import com.example.musiclibrary.models.Book;
import com.example.musiclibrary.models.Rental;
import com.example.musiclibrary.models.User;
import com.example.musiclibrary.repositories.BookRepository;
import com.example.musiclibrary.repositories.RentalRepository;
import com.example.musiclibrary.repositories.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("RentalServiceImplGrpc")
public class RentalServiceImpl extends RentalServiceGrpc.RentalServiceImplBase {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void getRental(RentalRequest request, StreamObserver<RentalResponse> responseObserver) {
        try {
            UUID rentalId = UUID.fromString(request.getId());
            Optional<Rental> rental = rentalRepository.findById(rentalId);
            if (rental.isPresent()) {
                var rent = rental.get();
                RentalResponse response = RentalResponse.newBuilder()
                        .setId(rent.getId() != null ? rent.getId().toString() : "")
                        .setRentalDate(rent.getRental_date() != null ? rent.getRental_date().toString() : "")
                        .setDueDate(rent.getDue_date() != null ? rent.getDue_date().toString() : "")
                        .setReturnDate(rent.getReturn_date() != null ? rent.getReturn_date().toString() : "")
                        .setExtendedTimes(Optional.ofNullable(rent.getExtended_times()).orElse(0))
                        .setIsReturned(Optional.ofNullable(rent.getIs_returned()).orElse(false))
                        .setUser(rent.getUser().getName())
                        .setBook(rent.getBook().getTitle())
                        .build();
                responseObserver.onNext(response);
            } else {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Запись аренды " + request.getId() + " не найдена")
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
    public void getAllRentals(EmptyRequest request, StreamObserver<RentalListResponse> responseObserver) {
        List<Rental> rentals = rentalRepository.findAll();
        List<RentalResponse> rentalResponses = rentals.stream()
                .map(rent -> RentalResponse.newBuilder()
                        .setId(rent.getId() != null ? rent.getId().toString() : "")
                        .setRentalDate(rent.getRental_date() != null ? rent.getRental_date().toString() : "")
                        .setDueDate(rent.getDue_date() != null ? rent.getDue_date().toString() : "")
                        .setReturnDate(rent.getReturn_date() != null ? rent.getReturn_date().toString() : "")
                        .setExtendedTimes(Optional.ofNullable(rent.getExtended_times()).orElse(0))
                        .setIsReturned(Optional.ofNullable(rent.getIs_returned()).orElse(false))
                        .setUser(rent.getUser().getName())
                        .setBook(rent.getBook().getTitle())
                        .build())
                .collect(Collectors.toList());

        RentalListResponse response = RentalListResponse.newBuilder()
                .addAllRentals(rentalResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addRental(RentalCheckRequest request, StreamObserver<RentalCheckResponse> responseObserver) {
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
                RentalCheckResponse response = RentalCheckResponse.newBuilder()
                        .setReceipt(receipt)
                        .setSuccess(false)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Уменьшить количество доступных копий
            book.setAvailable_copies(book.getAvailable_copies() - 1);
            bookRepository.save(book);

            // Создать запись аренды
            Rental rental = new Rental();
            rental.setBook(book);
            rental.setUser(user);
            rental.setRental_date(LocalDate.now());
            rentalRepository.save(rental);

            String receipt = generateReceipt(user, book, true, "Аренда успешно оформлена");
            RentalCheckResponse response = RentalCheckResponse.newBuilder()
                    .setReceipt(receipt)
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка на сервере").asRuntimeException());
        }
    }

    /**
     * Генерация документа-чека
     */
    private String generateReceipt(User user, Book book, boolean success, String message) {
        StringBuilder receiptBuilder = new StringBuilder();
        receiptBuilder.append("==== ЧЕК ОБ АРЕНДЕ ====\n");
        receiptBuilder.append("Время: ").append(LocalDateTime.now()).append("\n");
        receiptBuilder.append("Пользователь: ").append(user.getName()).append("\n");
        receiptBuilder.append("Книга: ").append(book.getTitle()).append("\n");
        receiptBuilder.append("Статус: ").append(success ? "Успешно" : "Отказано").append("\n");
        receiptBuilder.append("Сообщение: ").append(message).append("\n");
        receiptBuilder.append("================\n");
        return receiptBuilder.toString();
    }
}