package com.clmcat.qianyu.mall.backstage.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 登录成功返回（含 adminToken + 当前账号 permCodes）。 */
@Data
@Builder
public class AdminLoginVO {
    private String adminToken;
    private Long adminId;
    private String username;
    private String realName;
    private List<String> permCodes;
}
