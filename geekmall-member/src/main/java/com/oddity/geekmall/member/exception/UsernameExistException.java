package com.oddity.geekmall.member.exception;

/**
 * @author oddity
 * @create 2023-10-13 16:18
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("用户名存在");
    }
}
