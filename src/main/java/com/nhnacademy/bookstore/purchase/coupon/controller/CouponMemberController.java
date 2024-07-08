package com.nhnacademy.bookstore.purchase.coupon.controller;

import com.nhnacademy.bookstore.purchase.coupon.dto.CouponRegistorRequest;
import com.nhnacademy.bookstore.purchase.coupon.feign.dto.response.ReadCouponFormResponse;
import com.nhnacademy.bookstore.purchase.coupon.service.CouponMemberService;
import com.nhnacademy.bookstore.util.ApiResponse;
import jakarta.ws.rs.GET;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 맴버 쿠폰 컨트롤러.
 *
 * @author 김병우
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookstore/members")
public class CouponMemberController {
    private final CouponMemberService couponMemberService;

    /**
     * 맴버 쿠폰 전체 읽기.
     *
     * @param memberId 맴버아이디
     * @return 쿠폰폼dto 리스트
     */
    @GetMapping("/coupons")
    private ApiResponse<List<ReadCouponFormResponse>> readCoupons(
            @RequestHeader("Member-Id") Long memberId) {

        return ApiResponse.success(couponMemberService.readMemberCoupons(memberId));
    }

    /**
     * 맴버 쿠폰 전체 읽기.
     *
     * @param memberId 맴버아이디
     * @return 쿠폰폼dto 리스트
     */
    @PostMapping("/coupons")
    private ApiResponse<Long> registerCoupon(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody CouponRegistorRequest couponRegistorRequest) {

        return ApiResponse.success(couponMemberService.registorCoupon(couponRegistorRequest.code(), memberId));
    }


}
