package com.nhnacademy.bookstore.purchase.purchaseBook.service.impl;

import com.nhnacademy.bookstore.book.book.repository.BookRepository;
import com.nhnacademy.bookstore.book.book.service.BookService;
import com.nhnacademy.bookstore.book.book.service.impl.BookServiceImpl;
import com.nhnacademy.bookstore.book.bookCartegory.service.BookCategoryService;
import com.nhnacademy.bookstore.book.bookTag.dto.response.ReadBookByTagResponse;
import com.nhnacademy.bookstore.book.bookTag.dto.response.ReadTagByBookResponse;
import com.nhnacademy.bookstore.book.category.dto.response.CategoryParentWithChildrenResponse;
import com.nhnacademy.bookstore.entity.bookCategory.BookCategory;
import com.nhnacademy.bookstore.entity.category.Category;
import com.nhnacademy.bookstore.entity.purchaseBook.PurchaseBook;
import com.nhnacademy.bookstore.purchase.purchase.repository.PurchaseRepository;
import com.nhnacademy.bookstore.purchase.purchaseBook.dto.request.CreatePurchaseBookRequest;
import com.nhnacademy.bookstore.purchase.purchaseBook.dto.request.DeletePurchaseBookRequest;
import com.nhnacademy.bookstore.purchase.purchaseBook.exception.NotExistsBook;
import com.nhnacademy.bookstore.purchase.purchaseBook.exception.NotExistsPurchase;
import com.nhnacademy.bookstore.purchase.purchaseBook.exception.NotExistsPurchaseBook;
import com.nhnacademy.bookstore.purchase.purchaseBook.dto.request.ReadPurchaseIdRequest;
import com.nhnacademy.bookstore.purchase.purchaseBook.dto.request.UpdatePurchaseBookRequest;
import com.nhnacademy.bookstore.purchase.purchaseBook.dto.response.ReadBookByPurchase;
import com.nhnacademy.bookstore.purchase.purchaseBook.dto.response.ReadPurchaseBookResponse;
import com.nhnacademy.bookstore.purchase.purchaseBook.repository.PurchaseBookCustomRepository;
import com.nhnacademy.bookstore.purchase.purchaseBook.repository.PurchaseBookRepository;
import com.nhnacademy.bookstore.purchase.purchaseBook.service.PurchaseBookService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문-책 서비스
 *
 * @author 정주혁
 */
@Transactional
@Service
public class PurchaseBookServiceImpl implements PurchaseBookService {
	private final PurchaseBookRepository purchaseBookRepository;
	private final PurchaseRepository purchaseRepository;
	private final BookRepository bookRepository;
	private final PurchaseBookCustomRepository purchaseBookCustomRepository;

	@Autowired
	public PurchaseBookServiceImpl(PurchaseBookRepository purchaseBookRepository, PurchaseRepository purchaseRepository,
		BookRepository bookRepository, BookCategoryService bookCategoryService,
		PurchaseBookCustomRepository purchaseBookCustomRepository) {
		this.purchaseBookRepository = purchaseBookRepository;
		this.purchaseRepository = purchaseRepository;
		this.bookRepository = bookRepository;
		this.purchaseBookCustomRepository = purchaseBookCustomRepository;
	}

	/**
	 * 주문으로 해당 주문의 책들을 조회
	 *
	 * @param purchaseId 주문 id
	 * @return 해당 주문의 책 리스트를 반환
	 */
	@Override
	public Page<ReadPurchaseBookResponse> readBookByPurchaseResponses(long purchaseId, Pageable pageable) {

		return  purchaseBookCustomRepository.readBookPurchaseResponses(purchaseId, pageable);
	}

	/**
	 * bookId와 purchaseId로 수정한 주문-책을 조회한후 update
	 *
	 * @param updatePurchaseBookRequest bookId와 purchaseId,수정사항이 있는 requestDto
	 * @return 수정한 주문-책 Id
	 */
	@Override
	public Long updatePurchaseBook(UpdatePurchaseBookRequest updatePurchaseBookRequest) {
		PurchaseBook purchaseBook = purchaseBookRepository.findByPurchaseIdAndBookId(
			updatePurchaseBookRequest.purchaseId(), updatePurchaseBookRequest.bookId()).orElse(null);
		if (purchaseBook == null) {
			throw new NotExistsPurchaseBook();
		}
		purchaseBook.setQuantity(updatePurchaseBookRequest.quantity());
		purchaseBook.setPrice(updatePurchaseBookRequest.price());
		purchaseBookRepository.save(purchaseBook);
		return purchaseBook.getId();
	}

	/**
	 * 주문-책 생성, 책id로 책을 조회하고,주문id로 주문을 조회한다음 추가적인 사항 추가하여 생성
	 *
	 * @param createPurchaseBookRequest bookId와 purchaseId, 추가 사항이있는 requestDto
	 * @return 생성한 id 반환
	 */
	@Override
	public Long createPurchaseBook(CreatePurchaseBookRequest createPurchaseBookRequest) {
		PurchaseBook purchaseBook = new PurchaseBook(
			bookRepository.findById(createPurchaseBookRequest.bookId()).orElseThrow(NotExistsBook::new)
			, createPurchaseBookRequest.quantity()
			, createPurchaseBookRequest.price()
			, purchaseRepository.findById(createPurchaseBookRequest.purchaseId()).orElseThrow(NotExistsPurchase::new));

		return purchaseBookRepository.save(purchaseBook).getId();
	}

	/**
	 * 주문-책 삭제
	 *
	 * @param purchaseBookRequest 삭제할 주문-책id requestDto
	 */
	@Override
	public void deletePurchaseBook(DeletePurchaseBookRequest purchaseBookRequest) {
		purchaseBookRepository.deleteById(purchaseBookRequest.purchaseBookId());
	}

}
