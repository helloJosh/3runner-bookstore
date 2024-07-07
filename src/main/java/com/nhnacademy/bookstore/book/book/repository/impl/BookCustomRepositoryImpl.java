package com.nhnacademy.bookstore.book.book.repository.impl;

import com.nhnacademy.bookstore.book.book.dto.response.BookListResponse;
import com.nhnacademy.bookstore.book.book.dto.response.BookManagementResponse;
import com.nhnacademy.bookstore.book.book.dto.response.ReadBookResponse;
import com.nhnacademy.bookstore.book.book.repository.BookCustomRepository;
import com.nhnacademy.bookstore.entity.book.QBook;
import com.nhnacademy.bookstore.entity.bookImage.QBookImage;
import com.nhnacademy.bookstore.entity.bookImage.enums.BookImageType;
import com.nhnacademy.bookstore.entity.bookLike.QBookLike;
import com.nhnacademy.bookstore.entity.totalImage.QTotalImage;
import com.nhnacademy.bookstore.purchase.purchaseBook.exception.NotExistsBook;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 도서 커스텀 레포지토리입니다.
 *
 * @author 김은비
 */
@Slf4j
@Repository
public class BookCustomRepositoryImpl implements BookCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private static final QBook qBook = QBook.book;
    private static final QBookImage qBookImage = QBookImage.bookImage;
    private static final QTotalImage qTotalImage = QTotalImage.totalImage;
    private static final QBookLike qBookLike = QBookLike.bookLike;

    public BookCustomRepositoryImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    /**
     * 도서 리스트를 불러오는 쿼리입니다.
     *
     * @param pageable 페이지
     * @return 도서 리스트
     * @author 김은비
     */
    @Override
    public Page<BookListResponse> readBookList(Pageable pageable) {
        List<BookListResponse> content = jpaQueryFactory.select(
                        Projections.constructor(BookListResponse.class,
                                qBook.id,
                                qBook.title,
                                qBook.price,
                                qBook.sellingPrice,
                                qBook.author,
                                qTotalImage.url))
                .from(qBook)
                .leftJoin(qBookImage)
                .on(qBookImage.book.id.eq(qBook.id).and(qBookImage.type.eq(BookImageType.MAIN)))
                .leftJoin(qTotalImage)
                .on(qTotalImage.bookImage.id.eq(qBookImage.id))
                .leftJoin(qBookLike).on(qBookLike.book.id.eq(qBook.id))
                .groupBy(qBook.id, qBook.title, qBook.price, qBook.sellingPrice, qBook.author, qTotalImage.url)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSort(pageable.getSort()))
                .fetch();
        long total = Optional.ofNullable(
                jpaQueryFactory.select(qBook.count())
                        .from(qBook)
                        .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }


    /**
     * Spring Data JPA Sort 객체를 OrderSpecifier 배열로 변환하는 메서드입니다.
     * QueryDSL 쿼리에서 정렬을 적용할 수 있습니다.
     *
     * @param sort 정렬 기준을 나타내는 Sort 객체
     * @return 정렬 기준에 따라 정렬된 OrderSpecifier 배열
     * @throws IllegalArgumentException 정렬 기준이 잘못된 경우
     * @author 김은비
     */
    private OrderSpecifier<?>[] getSort(Sort sort) {
        return sort.stream()
                .map(order -> {
                    String property = order.getProperty();
                    boolean isAscending = order.isAscending();
                    log.info("Sorting by property: {}", property);
                    log.info("Is ascending: {}", isAscending);

                    switch (property) {
                        case "viewCount":
                            return new OrderSpecifier<>(
                                    isAscending ? Order.ASC : Order.DESC,
                                    qBook.viewCount);
                        case "likes":
                            return new OrderSpecifier<>(
                                    isAscending ? Order.ASC : Order.DESC,
                                    qBookLike.count());
                        case "publishedDate":
                            return new OrderSpecifier<>(
                                    isAscending ? Order.ASC : Order.DESC,
                                    qBook.publishedDate);
                        case "price":
                            return new OrderSpecifier<>(
                                    isAscending ? Order.ASC : Order.DESC,
                                    qBook.price);
                        default:
                            throw new IllegalArgumentException("정렬 기준이 잘못되었습니다!!: " + property);
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }


    /**
     * 도서 상세 보기 쿼리입니다.
     *
     * @param bookId 북 아이디
     * @return 도서 상세 정보
     * @author 한민기
     */
    @Override
    public ReadBookResponse readDetailBook(Long bookId) {
        List<ReadBookResponse> content = jpaQueryFactory.select(Projections.constructor(ReadBookResponse.class,
                        qBook.id,
                        qBook.title,
                        qBook.description,
                        qBook.publishedDate,
                        qBook.price,
                        qBook.quantity,
                        qBook.sellingPrice,
                        qBook.viewCount,
                        qBook.packing,
                        qBook.author,
                        qBook.isbn,
                        qBook.publisher,
                        qTotalImage.url))
                .from(qBook)
                .leftJoin(qBookImage)
                .on(qBookImage.book.id.eq(qBook.id).and(qBookImage.type.eq(BookImageType.MAIN)))
                .leftJoin(qTotalImage)
                .on(qTotalImage.bookImage.id.eq(qBookImage.id))
                .where(qBook.id.eq(bookId))
                .limit(1)
                .fetch();

        if (content.isEmpty()) {
            throw new NotExistsBook();
        }
        return content.getFirst();
    }

    /**
     * 관리자 페이지에서 도서 정보를 불러오는 쿼리입니다.
     *
     * @param pageable 페이지 객체
     * @return 도서 리스트
     * @author 한민기
     */
    @Override
    public Page<BookManagementResponse> readAdminBookList(Pageable pageable) {
        List<BookManagementResponse> content = jpaQueryFactory.select(
                        Projections.constructor(BookManagementResponse.class,
                                qBook.id,
                                qBook.title,
                                qBook.price,
                                qBook.sellingPrice,
                                qBook.author,
                                qBook.quantity,
                                qBook.viewCount))
                .from(qBook)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long total = Optional.ofNullable(
                jpaQueryFactory.select(qBook.count())
                        .from(qBook)
                        .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

}


