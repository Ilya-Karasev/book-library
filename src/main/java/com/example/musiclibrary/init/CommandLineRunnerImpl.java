package com.example.musiclibrary.init;
import com.example.musiclibrary.dtos.BookDto;
import com.example.musiclibrary.dtos.RentalDto;
import com.example.musiclibrary.dtos.ReservationDto;
import com.example.musiclibrary.dtos.UserDto;
import com.example.musiclibrary.rabbitmq.*;
import com.example.musiclibrary.services.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
public class CommandLineRunnerImpl implements CommandLineRunner {
    @Autowired
    private UserService userService;
    @Autowired
    private BookService bookService;
    @Autowired
    private RentalService rentalService;
    @Autowired
    private ReservationService reservationService;
    public CommandLineRunnerImpl(UserService userService, BookService bookService, RentalService rentalService, ReservationService reservationService) {
        this.userService = userService;
        this.bookService = bookService;
        this.rentalService = rentalService;
        this.reservationService = reservationService;
    }
    @Override
    public void run(String... args) throws Exception {
        seedData();
    }
    private void seedData() throws IOException, InterruptedException {
        UserDto librarian = userService.register(new UserDto("Anna Librarian", "anna@library.com", "LIBRARIAN", UserDto.Role.Librarian, LocalDate.of(2024, 1, 1), "8(800)555-35-35", null));
        UserDto user = userService.register(new UserDto("John Doe", "johndoe@example.com", "USER_JD", UserDto.Role.User, LocalDate.of(2003, 8, 19), "8(999)696-96-96", "Obraztsova street, 9"));
        BookDto book1 = bookService.addBook(new BookDto(librarian, "Effective Java", "Joshua Bloch", "Addison-Wesley", 2018, "Programming", 2, 5, "A must-read for Java developers"), librarian.getName());
        BookDto book2 = bookService.addBook(new BookDto(librarian, "Clean Code", "Robert C. Martin", "Prentice Hall", 2008,  "Programming", 1, 4, "Guide to writing clean, maintainable code"), librarian.getName());
//        RentalDto rental = rentalService.addRental(new RentalDto(user, book1, LocalDate.of(2024, 9, 10), LocalDate.of(2024, 9, 20), null, 0, false), user.getName(), book1.getTitle());
//        ReservationDto reservation = reservationService.addReservation(new ReservationDto(user, book2, LocalDate.of(2024, 9, 12), LocalDate.of(2024, 9, 19), true), user.getName(), book2.getTitle());
//        rentalService.editRental(rental.getId(), new RentalDto(user, book1, LocalDate.of(2024, 9, 10), LocalDate.of(2024, 9, 20), null, 0, true));
//        reservationService.editReservation(reservation.getId(), new ReservationDto(user, book2, LocalDate.of(2024, 9, 12), LocalDate.of(2024, 9, 19), false));
        System.out.println(rentalService.getAllRentals());
        System.out.println(reservationService.getAllReservations());
    }
}