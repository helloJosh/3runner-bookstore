package com.nhnacademy.bookstore.book.review.service.impl;

import com.nhnacademy.bookstore.book.book.exception.BookDoesNotExistException;
import com.nhnacademy.bookstore.book.book.repository.BookRepository;
import com.nhnacademy.bookstore.book.review.dto.request.CreateReviewRequest;
import com.nhnacademy.bookstore.book.review.dto.request.DeleteReviewRequest;
import com.nhnacademy.bookstore.book.review.dto.response.ReviewDetailResponse;
import com.nhnacademy.bookstore.book.review.dto.response.ReviewListResponse;
import com.nhnacademy.bookstore.book.review.dto.response.UserReadReviewResponse;
import com.nhnacademy.bookstore.book.review.exception.ReviewNotExistsException;
import com.nhnacademy.bookstore.book.review.exception.UnauthorizedReviewAccessException;
import com.nhnacademy.bookstore.book.review.repository.ReviewRepository;
import com.nhnacademy.bookstore.book.review.service.ReviewService;
import com.nhnacademy.bookstore.book.reviewLike.repository.ReviewLikeRepository;
import com.nhnacademy.bookstore.entity.member.Member;
import com.nhnacademy.bookstore.entity.review.Review;
import com.nhnacademy.bookstore.entity.review.enums.ReviewStatus;
import com.nhnacademy.bookstore.member.member.exception.MemberNotExistsException;
import com.nhnacademy.bookstore.member.member.repository.MemberRepository;
import com.nhnacademy.bookstore.purchase.purchaseBook.repository.PurchaseBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * 리뷰 서비스 구현체입니다.
 *
 * @author 김은비
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final PurchaseBookRepository purchaseBookRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    /**
     * 리뷰 생성 메서드입니다.
     *
     * @param purchaseBookId      주문-책 아이디
     * @param memberId            멤버 아이디
     * @param createReviewRequest 리뷰 생성 요청 dto
     * @return 생성된 리뷰 아이디
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Long createReview(long purchaseBookId, long memberId, CreateReviewRequest createReviewRequest) {
        if (!reviewRepository.existByPurchaseBook(purchaseBookId, memberId)) {
            throw new UnauthorizedReviewAccessException();
        }

        Review review = Review.builder()
                .purchaseBook(purchaseBookRepository.findById(purchaseBookId)
                        .orElseThrow(() -> new IllegalArgumentException("구매한 책을 찾을 수 없습니다.")))
                .title(createReviewRequest.title())
                .content(createReviewRequest.content())
                .rating(createReviewRequest.ratings())
                .build();

        reviewRepository.save(review);
        return review.getId();
    }

    /**
     * 리뷰 수정 메서드입니다.
     *
     * @param reviewId            수정할 리뷰 아이디
     * @param memberId            멤버 아이디
     * @param createReviewRequest 리뷰 업데이트 요청 dto
     * @return 수정된 리뷰 아이디
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Long updateReview(long reviewId, long memberId, CreateReviewRequest createReviewRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotExistsException::new);

        if (review.getPurchaseBook().getPurchase().getMember().getId() != memberId) {
            throw new UnauthorizedReviewAccessException();
        }

        review.setTitle(createReviewRequest.title());
        review.setContent(createReviewRequest.content());
        review.setRating(createReviewRequest.ratings());

        return review.getId();
    }

    /**
     * 리뷰 삭제 메서드입니다.
     * 관리자 권한 확인 후 삭제 진행
     *
     * @param reviewId            삭제할 리뷰 아이디
     * @param memberId            관리자 아이디
     * @param deleteReviewRequest 삭제 요청 dto
     * @return 삭제된 리뷰 아이디
     */
    @Override
    @Transactional
    public Long deleteReview(long reviewId, long memberId, DeleteReviewRequest deleteReviewRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotExistsException::new);

        // 관리자 권한 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotExistsException::new);
        boolean isAdmin = member.getMemberAuthList().stream()
                .anyMatch(auth -> "admin".equals(auth.getAuth().getName()));
        if (!isAdmin) {
            throw new UnauthorizedReviewAccessException();
        }
        review.setDeletedAt(ZonedDateTime.now());
        review.setDeletedReason(deleteReviewRequest.deleteReason());
        review.setReviewStatus(ReviewStatus.DELETE);

        return review.getId();
    }

    /**
     * 리뷰 상세 조회 메서드입니다.
     *
     * @param reviewId 조회할 리뷰 아이디
     * @return 리뷰 상세 정보
     */
    @Override
    public ReviewDetailResponse readDetailReview(long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotExistsException();
        }
        return reviewRepository.getReviewDetail(reviewId);
    }

    /**
     * 사용자에게 보여줄 리뷰 조회 (좋아요 포함) 메서드입니다.
     *
     * @param reviewId 리뷰 아이디
     * @return 리뷰 상세 정보
     */
    @Override
    @Transactional(readOnly = true)
    public UserReadReviewResponse readDetailUserReview(long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotExistsException();
        }
        ReviewDetailResponse reviewDetailResponse = readDetailReview(reviewId);
        long likeCount = reviewLikeRepository.countByReviewId(reviewId);
        return UserReadReviewResponse.builder()
                .bookId(reviewDetailResponse.bookId())
                .bookTitle(reviewDetailResponse.bookTitle())
                .reviewId(reviewDetailResponse.reviewId())
                .reviewTitle(reviewDetailResponse.reviewTitle())
                .reviewContent(reviewDetailResponse.reviewContent())
                .ratings(reviewDetailResponse.ratings())
                .memberEmail(reviewDetailResponse.memberEmail())
                .createdAt(reviewDetailResponse.createdAt())
                .updated(reviewDetailResponse.updated())
                .updatedAt(reviewDetailResponse.updatedAt())
                .reviewLike(likeCount)
                .build();
    }

    /**
     * 리뷰 전체 조회 메서드입니다.
     *
     * @param pageable 페이지 객체
     * @return 전체 조회 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> readAllReviews(Pageable pageable) {
        return reviewRepository.getReviewList(pageable);
    }

    /**
     * 책 아이디로 리뷰 조회하는 메서드입니다.
     *
     * @param bookId   책 아이디
     * @param pageable 페이지 객체
     * @return 리뷰 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> readAllReviewsByBookId(long bookId, Pageable pageable) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookDoesNotExistException("존재하지 않는 책입니다.");
        }
        return reviewRepository.getReviewsByBookId(bookId, pageable);
    }

    /**
     * 멤버 아이디로 리뷰 조회하는 메서드입니다.
     *
     * @param memberId 멤버 아이디
     * @param pageable 페이지 객체
     * @return 리뷰 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> readAllReviewsByMemberId(long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotExistsException();
        }
        return reviewRepository.getReviewsByUserId(memberId, pageable);
    }
}
