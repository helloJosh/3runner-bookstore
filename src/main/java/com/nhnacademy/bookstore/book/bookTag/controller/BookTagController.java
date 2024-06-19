package com.nhnacademy.bookstore.book.bookTag.controller;

import com.nhnacademy.bookstore.book.bookTag.dto.request.ReadBookIdRequest;
import com.nhnacademy.bookstore.book.bookTag.dto.response.ReadBookByTagResponse;
import com.nhnacademy.bookstore.book.bookTag.dto.request.ReadTagRequest;
import com.nhnacademy.bookstore.book.bookTag.dto.response.ReadTagByBookResponse;
import com.nhnacademy.bookstore.book.bookTag.exception.ReadBookTagNotFoundResponseException;
import com.nhnacademy.bookstore.book.bookTag.exception.ReadBookTagRequestFormException;
import com.nhnacademy.bookstore.book.bookTag.service.BookTagService;
import com.nhnacademy.bookstore.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Set;


/**
 * @author 정주혁
 *
 * booktagController
 */

@RestController
@RequestMapping
public class  BookTagController {
    @Autowired
    BookTagService bookTagService;


    /**
     * 태그값으로 책들을 가져오기 위한 메소드
     *
     * @param tagId 해당 태그가 달린 책을 가져오기 위한 태그 id, page를 설정할 size, 현재 page, 정렬할 sort 를 포함
     *
     * @return ApiResponse< Page<ReadBookByTagResponse>> 커스터마이징 한 헤더와 불러온 해당 태그가 달린 책들로 이루어진 바디를 합친 실행 값
     */
    @GetMapping("/tags/{tagId}/books")
    public ApiResponse< Page<ReadBookByTagResponse>> readBookByTagId(@Valid ReadTagRequest tagId,
                                                                    BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getField())
                            .append(": ")
                            .append(error.getDefaultMessage())
                            .append("\n")
            );
            throw new ReadBookTagRequestFormException(errorMessage.toString());
        }

        Pageable pageable;
        if(!tagId.sort().isEmpty()) {
            pageable = PageRequest.of(tagId.page(), tagId.page(), Sort.by(tagId.sort()));
        }
        else{
            pageable = PageRequest.of(tagId.page(), tagId.size());
        }

        Page<ReadBookByTagResponse> bookByTagResponsePage = bookTagService.readBookByTagId(tagId,pageable);
        if(bookByTagResponsePage.isEmpty()){

            throw new ReadBookTagNotFoundResponseException("책을 찾을수가 없습니다.");
        }

        return ApiResponse.success(bookByTagResponsePage);
    }


    /**
     * 책에 달린 태그들을 가져오기 위한 메소드
     * @param bookId 책에 달린 태그들을 가져오기 위한 책 id
     * @return ApiResponse< Page<ReadBookByTagResponse>> 커스터마이징 한 헤더와 불러온 해당 책에 달린 태그들로 이루어진 바디를 합친 실행 값
     */
    @GetMapping("/books/{bookId}/tags")
    public ApiResponse<Set<ReadTagByBookResponse>> readTagByBookId(@Valid ReadBookIdRequest bookId,
                                                                  BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getField())
                            .append(": ")
                            .append(error.getDefaultMessage())
                            .append("\n")
            );
            throw new ReadBookTagRequestFormException(errorMessage.toString());
        }
        Set<ReadTagByBookResponse> tags = bookTagService.readTagByBookId(bookId);
        if(tags.isEmpty()){
            throw new ReadBookTagNotFoundResponseException("Tag가 없습니다.");
        }

        return ApiResponse.success(tags);

    }}
