package com.nhnacademy.bookstore.book.book.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhnacademy.bookstore.book.book.dto.request.CreateBookRequest;
import com.nhnacademy.bookstore.book.book.dto.response.ReadBookResponse;
import com.nhnacademy.bookstore.book.book.exception.CreateBookRequestFormException;
import com.nhnacademy.bookstore.book.book.service.BookService;
import com.nhnacademy.bookstore.book.bookImage.service.BookImageService;
import com.nhnacademy.bookstore.entity.bookImage.enums.BookImageType;
import com.nhnacademy.bookstore.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 책 요청 컨트롤러.
 *
 * @author 김병우
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookstore/books")
public class BookController {
	private final BookService bookService;
	private final BookImageService bookImageService;

	/**
	 * 책 등록 요청 처리.
	 *
	 * @param createBookRequest request form
	 * @param bindingResult binding result
	 * @return ApiResponse<>
	 */
	@Transactional
	@PostMapping
	public ApiResponse<Void> createBook(@Valid @RequestBody CreateBookRequest createBookRequest,
		BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new CreateBookRequestFormException(bindingResult.getFieldErrors().toString());
		}
		long bookId = bookService.createBook(createBookRequest);
		//TODO 북 카테고리 서비스로 추가
		//TODO 북 태그 서비스로 추가
		bookImageService.createBookImage(createBookRequest.imageList(), bookId, BookImageType.DESCRIPTION);
		if (!Objects.isNull(createBookRequest.imageName())) {
			bookImageService.createBookImage(List.of(createBookRequest.imageName()), bookId, BookImageType.MAIN);
		}

		return new ApiResponse<Void>(new ApiResponse.Header(true, 201));
	}

	@GetMapping("/{bookId}")
	public ApiResponse<ReadBookResponse> readBook(@PathVariable("bookId") Long bookId) {
		ReadBookResponse book = bookService.readBookById(bookId);

		return new ApiResponse<ReadBookResponse>(
			new ApiResponse.Header(true, 200),
			new ApiResponse.Body<ReadBookResponse>(book)
		);
	}
}

