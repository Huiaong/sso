package com.huiaong.sso.redis;

import com.huiaong.sso.controller.vo.SsoUserVo;
import com.huiaong.sso.util.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static final String SSO_USER_TOKEN_TAG = "SSO_USER_TOKEN";

    @Test
    public void read(){
        String hello = redisTemplate.opsForValue().get(SSO_USER_TOKEN_TAG.concat(":").concat("ssoUserVo.getToken()"));
        System.out.println(hello);
    }

    @Test
    public void write(){
        SsoUserVo ssoUserVo = new SsoUserVo();
        ssoUserVo.setId(1L);
        ssoUserVo.setUsername("huiaong");
        String key = SSO_USER_TOKEN_TAG.concat(":").concat("ssoUserVo.getToken()");
        redisTemplate.opsForValue().set(key, JsonMapper.nonDefaultMapper().toJson(ssoUserVo), 30 * 60, TimeUnit.MINUTES);
    }

    @Test
    public void write1(){
        redisTemplate.opsForValue().set("key", "value", 30 * 60, TimeUnit.SECONDS);
    }
}
