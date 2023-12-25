package com.oddity.geekmall.auth.vo;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-10-14 22:43
 */

@Data
public class SocialUser {

    public String access_token;
    public String token_type;
    public long expires_in;
    public String refresh_token;
    public String scope;
    public long created_at;

    public String uid;
}
