package com.oddity.geekmall.auth;

import com.oddity.geekmall.auth.feign.ThirdPartFeignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GeekmallAuthServerApplicationTests {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Test
    void contextLoads() {
    }

    @Test
    public void testSms(){
        thirdPartFeignService.sendCode("18189533489", "111");
    }

}
