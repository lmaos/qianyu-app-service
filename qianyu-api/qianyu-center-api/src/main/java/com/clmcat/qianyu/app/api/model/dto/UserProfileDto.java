package com.clmcat.qianyu.app.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户基础信息（个人中心对外展示）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 头像 URL */
    private String avatar;

    /** 昵称 */
    private String nickname;

    /** 用户外显编号 */
    private String userNo;

    /** 个人签名 */
    private String signature;

    /** 所在地，格式：北京-北京 或 中国 或未知 */
    private String location;
}
