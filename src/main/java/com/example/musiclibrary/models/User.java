package com.example.musiclibrary.models;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;
import java.time.LocalDate;
import java.util.*;
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String name;
    private String email;
    private String password;
    public enum Role {
        User(0), Librarian(1);
        private final int roleNum;
        Role(int roleNum) {
            this.roleNum = roleNum;
        }
        public int getRoleNum() {
            return roleNum;
        }
    }
    @Enumerated(EnumType.ORDINAL)
    private Role role;
    private LocalDate membership_date;
    private String phone_number;
    private String address;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @Cascade({org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.REMOVE})
    private Set<Rental> rental;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @Cascade({org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.REMOVE})
    private Set<Reservation> reservation;
    public User(String name, String email, String password, Role role, LocalDate membership_date, String phone_number, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.membership_date = membership_date;
        this.phone_number = phone_number;
        this.address = address;
    }
    protected User(){
    }
    @Column(name = "Name", length = 100, nullable = false)
    public String getName() {
        return name;
    }
    @Column(name = "Email", length = 100, nullable = false, unique = true)
    public String getEmail() {
        return email;
    }
    @Column(name = "Password", nullable = false)
    public String getPassword() {
        return password;
    }
    private void setName(String name) {
        this.name = name;
    }
    private void setPassword(String password) {
        this.password = password;
    }
    @Column(name = "Role", length = 50, nullable = false)
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    @Column(name = "Membership Date", nullable = false)
    public LocalDate getMembership_date() {
        return membership_date;
    }
    public void setMembership_date(LocalDate membership_date) {
        this.membership_date = membership_date;
    }
    @Column(name = "Phone Number", length = 20)
    public String getPhone_number() {
        return phone_number;
    }
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
    @Column(name = "Address")
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    @Column(name = "Rentals")
    public Set<Rental> getRental() {
        return rental;
    }
    public void setRental(Set<Rental> rental) {
        this.rental = rental;
    }
    @Column(name = "Reservations")
    public Set<Reservation> getReservation() {
        return reservation;
    }
    public void setReservation(Set<Reservation> reservation) {
        this.reservation = reservation;
    }
}