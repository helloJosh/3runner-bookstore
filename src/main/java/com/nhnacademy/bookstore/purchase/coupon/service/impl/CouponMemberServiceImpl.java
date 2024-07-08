package com.nhnacademy.bookstore.purchase.coupon.service.impl;

import com.nhnacademy.bookstore.entity.coupon.Coupon;
import com.nhnacademy.bookstore.entity.coupon.enums.CouponStatus;
import com.nhnacademy.bookstore.entity.member.Member;
import com.nhnacademy.bookstore.member.member.exception.MemberNotExistsException;
import com.nhnacademy.bookstore.member.member.repository.MemberRepository;
import com.nhnacademy.bookstore.purchase.coupon.exception.CouponDoesNotExistException;
import com.nhnacademy.bookstore.purchase.coupon.exception.CouponNotAllowedException;
import com.nhnacademy.bookstore.purchase.coupon.feign.CouponControllerClient;
import com.nhnacademy.bookstore.purchase.coupon.feign.dto.request.CreateCouponFormRequest;
import com.nhnacademy.bookstore.purchase.coupon.feign.dto.request.ReadCouponFormRequest;
import com.nhnacademy.bookstore.purchase.coupon.feign.dto.response.ReadCouponFormResponse;
import com.nhnacademy.bookstore.purchase.coupon.repository.CouponRepository;
import com.nhnacademy.bookstore.purchase.coupon.service.CouponMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 쿠폰맴버서비스 구현체.
 *
 * @author 김병우
 */
@Transactional
@Service
@RequiredArgsConstructor
public class CouponMemberServiceImpl implements CouponMemberService {
    private final CouponControllerClient couponControllerClient;
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    /**
     * 맴버 쿠폰 전체 읽기.
     *
     * @param memberId 맴버아이디
     * @return 쿠폰폼dto 리스트
     */
    @Override
    public List<ReadCouponFormResponse> readMemberCoupons(Long memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(MemberNotExistsException::new);

        List<Coupon> coupons = couponRepository.findByMemberAndCouponStatus(member, CouponStatus.READY);

        List<Long> couponFormIds = new ArrayList<>();

        for (Coupon c : coupons) {
            couponFormIds.add(c.getCouponFormId());
        }

        return couponControllerClient
                .readCouponForm(ReadCouponFormRequest.builder().couponFormIds(couponFormIds).build())
                .getBody()
                .getData();
    }

    /**
     * 쿠폰 사용.
     *
     * @param couponFormId 쿠폰아이디
     * @param memberId 맴버아이디
     * @return 쿠폰아이디
     */
    @Override
    public Long useCoupons(Long couponFormId, Long memberId) {
        Coupon coupon = couponRepository
                .findCouponByCouponFormId(couponFormId)
                .orElseThrow(()-> new CouponDoesNotExistException(couponFormId + "쿠폰이 존재하지않습니다."));

        if (!Objects.equals(coupon.getMember().getId(), memberId)) {
            throw new CouponNotAllowedException(couponFormId + "쿠폰이 권한이 없습니다.");
        }

        couponRepository.updateCouponStatus(CouponStatus.USED, couponFormId);

        return couponFormId;
    }

    //TODO : 환불 구현시 필요
    @Override
    public Long refundCoupons(Long couponId, Long memberId) {

        return couponId;
    }

    /**
     * 생일쿠폰 발급.
     * 스케줄러로 발급
     */
    @Async
    @Scheduled(cron = "0 0 13 * * ?")
    @Override
    public void issueBirthdayCoupon() {
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            if (member.getBirthday().getDayOfYear() == ZonedDateTime.now().getDayOfYear()) {
                CreateCouponFormRequest couponFormRequest = CreateCouponFormRequest.builder()
                        .startDate(ZonedDateTime.now())
                        .endDate(ZonedDateTime.now().plusDays(30))
                        .name("Birthday Coupon")
                        .maxPrice(100000)
                        .minPrice(10000)
                        .couponTypeId(5L)
                        .couponUsageId(3L)
                        .build();

                couponControllerClient.createCouponForm(couponFormRequest);

                Long couponFormId = couponControllerClient.createCouponForm(couponFormRequest).getBody().getData();

                Coupon coupon = new Coupon(
                        couponFormId,
                        CouponStatus.READY,
                        member
                );

                couponRepository.save(coupon);
            }
        }
    }

    /**
     * 웰컴 쿠폰 발급 메소드.
     *
     * @param member 맴버
     */
    @Override
    public void issueWelcomeCoupon(Member member) {
        CreateCouponFormRequest couponFormRequest = CreateCouponFormRequest.builder()
                .startDate(ZonedDateTime.now())
                .endDate(ZonedDateTime.now().plusDays(365))
                .name("Welcome Coupon")
                .maxPrice(100000)
                .minPrice(10000)
                .couponTypeId(5L)
                .couponUsageId(3L)
                .build();

        couponControllerClient.createCouponForm(couponFormRequest);

        Long couponFormId = couponControllerClient.createCouponForm(couponFormRequest).getBody().getData();

        Coupon coupon = new Coupon(
                couponFormId,
                CouponStatus.READY,
                member
        );

        couponRepository.save(coupon);
    }

    @Override
    public Long readCoupon(Long couponFormId) {
        Coupon coupon = couponRepository.findCouponByCouponFormId(couponFormId).orElseThrow(()->new CouponDoesNotExistException(couponFormId+" 해당 쿠폰 폼 아이디가 없습니다."));
        return coupon.getId();
    }

    @Override
    public Long registorCoupon(String code, Long memberId) {
        List<ReadCouponFormResponse> responses = couponControllerClient.readAllCouponForms().getBody().getData();

        ReadCouponFormResponse matchingCoupon = responses.stream()
                .filter(response -> code.equals(response.code().toString()))
                .findFirst()
                .orElseThrow(()-> new CouponDoesNotExistException("쿠폰이 없습니다."));

        Coupon coupon = new Coupon(
                matchingCoupon.couponFormId(),
                CouponStatus.READY,
                memberRepository.findById(memberId).orElseThrow(MemberNotExistsException::new)
        );

        couponRepository.save(coupon);

        return coupon.getId();
    }
}
