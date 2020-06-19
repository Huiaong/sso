package com.huiaong.sso.controller;

import com.huiaong.sso.controller.vo.SsoUserVo;
import com.huiaong.sso.model.SsoUser;
import com.huiaong.sso.util.JsonMapper;
import com.huiaong.sso.util.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class SsoController {

    private static final String COOKIE_USER_TOKEN = "cookie_user_token";
    private static final String SSO_USER_TOKEN_TAG = "SSO_USER_TOKEN";
    private final StringRedisTemplate redisTemplate;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void login(String redirectUrl,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        log.info("get login in");
        String token = getToken(request);
        val ssoUserVo = verifyUserTicket(token);
        if (!Objects.isNull(ssoUserVo)) {
            // 返回
            response.sendRedirect(redirectUrl + "?token=" + ssoUserVo.getToken());
        } else {
            response.sendRedirect("http://www.sso.com?redirectUrl=" + redirectUrl);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Response<SsoUserVo> doLogin(@Validated @NotBlank(message = "用户名不能为空") String username,
                                       @NotBlank(message = "密码不能为空") String password,
                                       @NotBlank(message = "重定向URL不能为空") String redirectUrl,
                                       HttpServletResponse response) {
        log.info("post login in");
        // 假设登录成功

        val id = System.currentTimeMillis();

        SsoUser ssoUser = new SsoUser();
        ssoUser.setId(id);
        ssoUser.setUsername(username);
        ssoUser.setPassword(password);

        // 生成 Token
        val token = UUID.randomUUID().toString().replace("-", "");

        // 封装 userVO
        SsoUserVo ssoUserVo = new SsoUserVo();
        BeanUtils.copyProperties(ssoUser, ssoUserVo);
        ssoUserVo.setToken(token);

        // redis 存储 Token
        storeToken(ssoUserVo, response);

        // 返回
        String url = redirectUrl + "?token=" + token;
        ssoUserVo.setRedirectUrl(url);
        return Response.ok(ssoUserVo);
    }

    @ResponseBody
    @RequestMapping(value = "logout", method = RequestMethod.POST)
    public Response<?> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("logout in");
        String token = getToken(request);
        // 清除全局 token 信息
        clearToken(token, response);

        return Response.ok();
    }

    @ResponseBody
    @RequestMapping(value = "/verifyToken", method = RequestMethod.POST)
    public Response<SsoUserVo> verifyToken(@Validated @NotBlank(message = "Token不能为空") String token,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        log.info("verifyToken in");
        SsoUserVo ssoUserVo = getUser(token);
        if (Objects.isNull(ssoUserVo)) return Response.fail("token is not exist");

        // 验证成功，返回用户会话信息
        return Response.ok(ssoUserVo);
    }

    private SsoUserVo verifyUserTicket(String token) {
        // 1.验证全局门票是否存在
        if (StringUtils.isBlank(token)) {
            return null;
        }

        // 2.验证全局门票绑定的用户会话信息是否存在
        String userVO = redisTemplate.opsForValue().get(SSO_USER_TOKEN_TAG.concat(":").concat(token));
        return JsonMapper.nonDefaultMapper().fromJson(userVO, SsoUserVo.class);
    }

    private void storeToken(SsoUserVo ssoUserVo, HttpServletResponse response) {
        redisTemplate.opsForValue().set(SSO_USER_TOKEN_TAG.concat(":").concat(ssoUserVo.getToken()), JsonMapper.nonDefaultMapper().toJson(ssoUserVo), 30 * 60, TimeUnit.SECONDS);
        Cookie cookie = new Cookie(COOKIE_USER_TOKEN, ssoUserVo.getToken());
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void clearToken(String token, HttpServletResponse response) {
        redisTemplate.delete(SSO_USER_TOKEN_TAG.concat(":").concat(token));
        Cookie cookie = new Cookie(COOKIE_USER_TOKEN, null);
        cookie.setDomain("cas.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

    private String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) return null;

        String cookieValue = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_USER_TOKEN)) {
                cookieValue = cookie.getValue();
                break;
            }
        }
        return cookieValue;
    }

    private SsoUserVo getUser(String token) {
        String userVO = redisTemplate.opsForValue().get(SSO_USER_TOKEN_TAG.concat(":").concat(token));
        if (StringUtils.isBlank(userVO)) return null;

        return JsonMapper.nonDefaultMapper().fromJson(userVO, SsoUserVo.class);
    }
}

