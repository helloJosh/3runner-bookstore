package com.nhnacademy.bookstore.purchase.purchaseCoupon.service.impl;

import com.nhnacademy.bookstore.entity.coupon.Coupon;
import com.nhnacademy.bookstore.entity.purchase.Purchase;
import com.nhnacademy.bookstore.entity.purchaseCoupon.PurchaseCoupon;
import com.nhnacademy.bookstore.purchase.coupon.exception.CouponDoesNotExistException;
import com.nhnacademy.bookstore.purchase.coupon.repository.CouponRepository;
import com.nhnacademy.bookstore.purchase.purchase.exception.PurchaseDoesNotExistException;
import com.nhnacademy.bookstore.purchase.purchase.repository.PurchaseRepository;
import com.nhnacademy.bookstore.purchase.purchaseCoupon.dto.ReadPurchaseCouponResponse;
import com.nhnacademy.bookstore.purchase.purchaseCoupon.repository.PurchaseCouponRepository;
import com.nhnacademy.bookstore.purchase.purchaseCoupon.service.PurchaseCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 주문쿠폰 서비스 구현체.
 *
 * @author 김병우
 */
@Transactional
@Service
@RequiredArgsConstructor
public class PurchaseCouponServiceImpl implements PurchaseCouponService {
    private final PurchaseRepository purchaseRepository;
    private final CouponRepository couponRepository;
    private final PurchaseCouponRepository purchaseCouponRepository;

    /**
     * 주문쿠폰 찾기 로직
     *
     * @param purchaseId 주문아이디
     * @return 주문쿠폰Dto 리스트
     */
    @Override
    public List<ReadPurchaseCouponResponse> read(Long purchaseId) {
        Purchase purchase = purchaseRepository
                .findById(purchaseId)
                .orElseThrow(()->new PurchaseDoesNotExistException(purchaseId + "주문이 없습니다."));

        return purchaseCouponRepository.findAllByPurchase(purchase).stream()
                .map(o -> ReadPurchaseCouponResponse.builder()
                        .purchaseCouponId(o.getId())
                        .couponId(o.getCoupon().getId())
                        .purchaseId(o.getPurchase().getId())
                        .status(o.getStatus().toString())
                        .discountPrice(o.getDiscountPrice())
                        .build()
                ).toList();
    }

    /**
     * 주문쿠폰 만들기.
     *
     * @param purchaseId 주문아이디
     * @param couponFormId 쿠폰폼아이디
     * @param discountPrice 할인가격
     * @return 주문쿠폰 아이디
     */
    @Override
    public Long create(Long purchaseId, Long couponFormId, int discountPrice) {
        Purchase purchase = purchaseRepository
                .findById(purchaseId)
                .orElseThrow(()->new PurchaseDoesNotExistException(purchaseId + "주문이 없습니다."));


        Coupon coupon = couponRepository
                .findCouponByCouponFormId(couponFormId)
                .orElseThrow(()->new CouponDoesNotExistException(couponFormId + "쿠폰이 없습니다."));


        PurchaseCoupon purchaseCoupon = new PurchaseCoupon(discountPrice, (short)0, coupon, purchase);

        purchaseCouponRepository.save(purchaseCoupon);

        return purchaseCoupon.getId();
    }
}
