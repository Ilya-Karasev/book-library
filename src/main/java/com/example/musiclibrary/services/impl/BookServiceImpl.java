package com.example.musiclibrary.services.impl;

import com.example.musiclibrary.dtos.BookDto;
import com.example.musiclibrary.dtos.UserDto;
import com.example.musiclibrary.dtos.show.BookShow;
import com.example.musiclibrary.models.Book;
import com.example.musiclibrary.models.User;
import com.example.musiclibrary.rabbitmq.*;
import com.example.musiclibrary.repositories.BookRepository;
import com.example.musiclibrary.repositories.UserRepository;
import com.example.musiclibrary.services.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private BookMessageSender bookSender;
    @Override
    public BookDto addBook(BookDto book, String user) throws InterruptedException {
        if (!bookRepository.existsByTitle(book.getTitle())) {
            Book b = modelMapper.map(book, Book.class);
            User u = userRepository.findByName(user).get();
            b.setUser(u);
            b.setCreated(LocalDateTime.now());
            b.setModified(LocalDateTime.now());
            bookSender.sendBookMessage("Книга " + b.getTitle() + " был зарегистрирована");
            return modelMapper.map(bookRepository.save(b), BookDto.class);
        } else return null;
    }

    @Override
    public Optional<BookShow> findBook(String title) throws InterruptedException {
        bookSender.sendBookMessage("Поиск книги " + title + " (" + modelMapper.map(bookRepository.findByTitle(title), BookDto.class).getId() + ")");
        return Optional.ofNullable(modelMapper.map(bookRepository.findByTitle(title), BookShow.class));
    }

    @Override
    public Optional<BookDto> findBookDto(String title) throws InterruptedException {
        bookSender.sendBookMessage("Поиск DTO-объекта книги (" + modelMapper.map(bookRepository.findByTitle(title), BookDto.class).getId() + ")");
        return Optional.ofNullable(modelMapper.map(bookRepository.findByTitle(title), BookDto.class));
    }

    @Override
    public List<BookShow> getAllBooks() throws InterruptedException {
        bookSender.sendBookMessage("Вывод всех записей о книгах");
        return bookRepository.findAll().stream().map((b) -> modelMapper.map(b, BookShow.class)).collect(Collectors.toList());
    }

    @Override
    public Optional<BookDto> editBook(String title, BookDto book) throws InterruptedException {
        BookDto b = modelMapper.map(bookRepository.findByTitle(title), BookDto.class);
        b.setTitle(book.getTitle());
        b.setAuthor(book.getAuthor());
        b.setDescription(book.getDescription());
        b.setAvailable_copies(b.getAvailable_copies());
        b.setGenre(book.getGenre());
        b.setPublication_year(book.getPublication_year());
        b.setPublisher(b.getPublisher());
        b.setTotal_copies(book.getTotal_copies());
        b.setModified(LocalDateTime.now());
        bookRepository.save(modelMapper.map(b, Book.class));
        bookSender.sendBookMessage("Редактирование записи книги (" + modelMapper.map(bookRepository.findByTitle(title), BookDto.class).getId() + ")");
        return Optional.ofNullable(modelMapper.map(bookRepository.findByTitle(b.getTitle()), BookDto.class));
    }

    @Override
    public void deleteBook(String title) throws InterruptedException {
        bookSender.sendBookMessage("Книга " + title + " (" + modelMapper.map(bookRepository.findByTitle(title), Book.class).getId() + " был удалена");
        bookRepository.delete(modelMapper.map(bookRepository.findByTitle(title), Book.class));
    }
}