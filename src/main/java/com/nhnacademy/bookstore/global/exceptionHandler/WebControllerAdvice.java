package com.nhnacademy.bookstore.global.exceptionHandler;
import com.nhnacademy.bookstore.book.book.exception.CreateBookRequestFormException;
import com.nhnacademy.bookstore.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

/**
 * Error Handler Controller
 * @author 김병우
 */
@RestControllerAdvice
public class WebControllerAdvice {

    // 응답, 성공,실패 폼 맞추기
    /**
     * INTERNAL_SERVER_ERROR 처리 메소드
     * @param ex
     * @param model
     * @return ApiResponse<ErrorResponseForm>
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<ErrorResponseForm> runtimeExceptionHandler(Exception ex, Model model) {
        return new ApiResponse<>(
                new ApiResponse.Header(false, 500, "server error"),
                new ApiResponse.Body<ErrorResponseForm>(ErrorResponseForm.builder()
                                        .title(ex.getMessage())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .timestamp(ZonedDateTime.now().toString())
                                        .build())
        );
    }

    /**
     * BAD_REQUEST 처리 메소드
     * @param ex
     * @param model
     * @return ApiResponse<ErrorResponseForm>
     */
    @ExceptionHandler({
            CreateBookRequestFormException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponseForm> badRequestHandler(Exception ex, Model model) {
        return new ApiResponse<>(
                new ApiResponse.Header(false, 400, "bad request"),
                new ApiResponse.Body<>(ErrorResponseForm.builder()
                        .title(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(ZonedDateTime.now().toString())
                        .build())
        );
    }


//    @ExceptionHandler({})
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ApiResponse<ErrorResponseForm> notFoundHandler(Exception ex, Model model) {
//        return new ApiResponse<>(
//                new ApiResponse.Header(false, 404, "not found"),
//                new ApiResponse.Body<>(ErrorResponseForm.builder()
//                        .title(ex.getMessage())
//                        .status(HttpStatus.NOT_FOUND.value())
//                        .timestamp(ZonedDateTime.now().toString())
//                        .build())
//        );
//    }
//
//
//    @ExceptionHandler({})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ApiResponse<ErrorResponseForm> unauthorizedHandler(Exception ex, Model model) {
//        return new ApiResponse<>(
//                new ApiResponse.Header(false, 405, "unauthorized"),
//                new ApiResponse.Body<>(ErrorResponseForm.builder()
//                        .title(ex.getMessage())
//                        .status(HttpStatus.UNAUTHORIZED.value())
//                        .timestamp(ZonedDateTime.now().toString())
//                        .build())
//        );
//    }
}
