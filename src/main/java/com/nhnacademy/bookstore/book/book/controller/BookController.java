package com.nhnacademy.bookstore.book.book.controller;

import com.nhnacademy.bookstore.book.book.dto.request.ReadBookResponse;
import com.nhnacademy.bookstore.book.book.exception.CreateBookRequestFormException;
import com.nhnacademy.bookstore.book.book.service.BookService;
import com.nhnacademy.bookstore.entity.book.Book;
import com.nhnacademy.bookstore.book.book.dto.request.CreateBookRequest;
import com.nhnacademy.bookstore.global.exceptionHandler.ErrorResponseForm;
import com.nhnacademy.bookstore.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

/**
 * 책 요청 컨트롤러.
 *
 * @author 김병우
 */
@RestController
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    /**
     * 책 등록 요청 처리.
     *
     * @param createBookRequest request form
     * @param bindingResult binding result
     * @return ApiResponse<>
     */
    @PostMapping("/book")
    public ApiResponse<Void> createBook(@Valid @RequestBody CreateBookRequest createBookRequest,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new CreateBookRequestFormException(bindingResult.getFieldErrors().toString());
        }

        // controller 쪽은 entity X -> OSIV(Open session in view) view에 오기전에 닫아버리기? -> service에서 처리
        bookService.createBook(createBookRequest);

        return new ApiResponse<Void>(new ApiResponse.Header(true, 201, "book created"));
    }

    @GetMapping("/book/{bookId}")
    public ApiResponse<ReadBookResponse> readBook(@PathVariable("bookId") Long bookId) {
        Book book = bookService.readBookById(bookId);

        return new ApiResponse<ReadBookResponse>(
                new ApiResponse.Header(true, 200, "Book found"),
                new ApiResponse.Body<ReadBookResponse>(ReadBookResponse.builder()
                        .id(bookId)
                        .title(book.getTitle())
                        .description(book.getDescription())
                        .publishedDate(book.getPublishedDate())
                        .price(book.getPrice())
                        .quantity(book.getQuantity())
                        .sellingPrice(book.getSellingPrice())
                        .viewCount(book.getViewCount())
                        .packing(book.isPacking())
                        .author(book.getAuthor())
                        .isbn(book.getIsbn())
                        .publisher(book.getPublisher())
                        .createdAt(book.getCreatedAt())
                        .build())
        );
    }
}

