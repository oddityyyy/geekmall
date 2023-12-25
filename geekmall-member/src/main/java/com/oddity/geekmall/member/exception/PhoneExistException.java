package com.oddity.geekmall.member.exception;

/**
 * @author oddity
 * @create 2023-10-13 16:17
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号存在");
    }
}
