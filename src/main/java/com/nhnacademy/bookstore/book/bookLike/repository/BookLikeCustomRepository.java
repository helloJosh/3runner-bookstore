package com.nhnacademy.bookstore.book.bookLike.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nhnacademy.bookstore.book.book.dto.response.BookListResponse;

/**
 * 도서-좋아요 custom repository.
 * @author 김은비
 */
public interface BookLikeCustomRepository {

	/**
	 * 회원이 좋아요한 도서 목록.
	 *
	 * @param memberId 회원 아이디
	 * @param pageable 페이지
	 * @return 도서 리스트
	 */
	Page<BookListResponse> findBookLikeByMemberId(long memberId, Pageable pageable);

	/**
	 * 도서의 좋아요 갯수.
	 *
	 * @param bookId 도서 아이디
	 * @return 좋아요 갯수
	 */
	long countLikeByBookId(long bookId);

	/**
	 * 좋아요가 많은 순으로 도서 조회.
	 *
	 * @param pageable 페이지
	 * @return 도서 리스트
	 */
	Page<BookListResponse> findBooksOrderByLikes(Pageable pageable);
}
