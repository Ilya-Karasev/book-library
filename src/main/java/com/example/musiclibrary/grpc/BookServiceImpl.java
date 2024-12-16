package com.example.musiclibrary.grpc;

import com.example.musiclibrary.*;
import com.example.musiclibrary.models.Book;
import com.example.musiclibrary.repositories.BookRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("bookServiceImplGrpc")
public class BookServiceImpl extends BookServiceGrpc.BookServiceImplBase {
    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void findBook(BookTitleRequest request, StreamObserver<BookResponse> responseObserver) {
        Optional<Book> bookOptional = bookRepository.findByTitle(request.getTitle());
        if (bookOptional.isPresent()) {
            var book = bookOptional.get();
            BookResponse response = BookResponse.newBuilder()
                    .setTitle(book.getTitle() != null ? book.getTitle() : "null")
                    .setAuthor(book.getAuthor() != null ? book.getAuthor() : "null")
                    .setPublisher(book.getPublisher() != null ? book.getPublisher() : "null")
                    .setPublicationYear(book.getPublication_year() != null ? book.getPublication_year() : null)
                    .setGenre(book.getGenre() != null ? book.getGenre() : "null")
                    .setDescription(book.getDescription() != null ? book.getDescription() : "null")
                    .setAvailableCopies(Optional.ofNullable(book.getAvailable_copies()).orElse(0))
                    .build();
            responseObserver.onNext(response);
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Книга под названием " + request.getTitle() + " не найдена")
                    .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAllBooks(EmptyRequest request, StreamObserver<BookListResponse> responseObserver) {
        List<Book> books = bookRepository.findAll();
        List<BookResponse> bookResponses = books.stream()
                .map(book -> BookResponse.newBuilder()
                        .setTitle(book.getTitle() != null ? book.getTitle() : "null")
                        .setAuthor(book.getAuthor() != null ? book.getAuthor() : "null")
                        .setPublisher(book.getPublisher() != null ? book.getPublisher() : "null")
                        .setPublicationYear(book.getPublication_year() != null ? book.getPublication_year() : null)
                        .setGenre(book.getGenre() != null ? book.getGenre() : "null")
                        .setDescription(book.getDescription() != null ? book.getDescription() : "null")
                        .setAvailableCopies(Optional.ofNullable(book.getAvailable_copies()).orElse(0))
                        .build())
                .collect(Collectors.toList());

        BookListResponse response = BookListResponse.newBuilder()
                .addAllBooks(bookResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}