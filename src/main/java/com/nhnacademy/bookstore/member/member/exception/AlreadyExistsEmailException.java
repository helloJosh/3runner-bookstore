package com.nhnacademy.bookstore.member.member.exception;

public class AlreadyExistsEmailException extends RuntimeException {
    public AlreadyExistsEmailException() {super("이미 존재하는 이메일입니다.");}
}
