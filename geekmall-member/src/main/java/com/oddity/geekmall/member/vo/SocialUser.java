package com.oddity.geekmall.member.vo;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-10-14 22:43
 */

@Data
public class SocialUser {

    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;

    private String uid;
}
