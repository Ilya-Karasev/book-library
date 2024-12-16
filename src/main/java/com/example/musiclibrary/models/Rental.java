package com.example.musiclibrary.models;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;
import java.time.LocalDate;
@Entity
@Table(name = "rentals")
public class Rental extends BaseEntity {
    private LocalDate rental_date;
    private LocalDate due_date;
    private LocalDate return_date;
    private Integer extended_times;
    private Boolean is_returned;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable=false)
    @Cascade(org.hibernate.annotations.CascadeType.MERGE)
    private User user;
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable=false)
    @Cascade(org.hibernate.annotations.CascadeType.MERGE)
    private Book book;
    public Rental(LocalDate rental_date, LocalDate due_date, LocalDate return_date, Integer extended_times, Boolean is_returned) {
        this.rental_date = rental_date;
        this.due_date = due_date;
        this.return_date = return_date;
        this.extended_times = extended_times;
        this.is_returned = is_returned;
    }
    public Rental() {
    }
    @Column(name = "Reservation Date")
    public LocalDate getRental_date() {
        return rental_date;
    }
    public void setRental_date(LocalDate rental_date) {
        this.rental_date = rental_date;
    }
    @Column(name = "Due Date")
    public LocalDate getDue_date() {
        return due_date;
    }
    public void setDue_date(LocalDate due_date) {
        this.due_date = due_date;
    }
    @Column(name = "Return Date")
    public LocalDate getReturn_date() {
        return return_date;
    }
    public void setReturn_date(LocalDate return_date) {
        this.return_date = return_date;
    }
    @Column(name = "Extended Times", columnDefinition = "0")
    public Integer getExtended_times() {
        return extended_times;
    }
    public void setExtended_times(Integer extended_times) {
        this.extended_times = extended_times;
    }
    @Column(name = "Returned?", columnDefinition = "FALSE")
    public Boolean getIs_returned() {
        return is_returned;
    }
    public void setIs_returned(Boolean is_returned) {
        this.is_returned = is_returned;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Book getBook() {
        return book;
    }
    public void setBook(Book book) {
        this.book = book;
    }
}