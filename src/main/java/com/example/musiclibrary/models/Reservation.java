package com.example.musiclibrary.models;
import java.time.LocalDate;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {
    private LocalDate reservation_date;
    private LocalDate expiry_date;
    private Boolean is_active;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable=false)
    @Cascade(org.hibernate.annotations.CascadeType.MERGE)
    private User user;
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable=false)
    @Cascade(org.hibernate.annotations.CascadeType.MERGE)
    private Book book;
    public Reservation(LocalDate reservation_date, LocalDate expiry_date, Boolean is_active) {
        this.reservation_date = reservation_date;
        this.expiry_date = expiry_date;
        this.is_active = is_active;
    }
    public Reservation() {
    }
    @Column(name = "Reservation Date")
    public LocalDate getReservation_date() {
        return reservation_date;
    }
    public void setReservation_date(LocalDate reservation_date) {
        this.reservation_date = reservation_date;
    }
    @Column(name = "Expiry Date")
    public LocalDate getExpiry_date() {
        return expiry_date;
    }
    public void setExpiry_date(LocalDate expiry_date) {
        this.expiry_date = expiry_date;
    }
    @Column(name = "Active", columnDefinition = "TRUE")
    public Boolean getIs_active() {
        return is_active;
    }
    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
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