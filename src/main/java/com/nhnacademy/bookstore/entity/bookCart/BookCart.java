package com.nhnacademy.bookstore.entity.bookCart;

import com.nhnacademy.bookstore.entity.book.Book;
import com.nhnacademy.bookstore.entity.cart.Cart;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Getter@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Min(0)
    @Column(nullable = false, columnDefinition = "int default 0")
    private int quantity;

    @NotNull
    private ZonedDateTime createdAt;


    // 연결
    @ManyToOne
    private Book book;

    @ManyToOne
    private Cart cart;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
    }

    public BookCart(int quantity, ZonedDateTime createdAt, Book book, Cart cart) {
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.book = book;
        this.cart = cart;
    }

    public BookCart(int quantity, Book book, Cart cart) {
        this.quantity = quantity;
        this.book = book;
        this.cart = cart;
    }
}
