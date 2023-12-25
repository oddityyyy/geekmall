package com.oddity.geekmall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author oddity
 * @create 2023-10-16 21:57
 */

@Data
public class UserInfoTo {

    private Long userId;
    private String userKey;

    private boolean tempUser = false;
}
