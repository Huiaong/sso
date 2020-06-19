package com.huiaong.sso.controller.vo;

import lombok.Data;

@Data
public class SsoUserVo {

    private Long id;

    private String username;

    private String token;

    private String redirectUrl;
}
