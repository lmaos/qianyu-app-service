package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营日志查询请求（/api/admin/oplog/page、/api/admin/loginlog/page）。
 * <p>startTime/endTime 为毫秒时间戳（Long），与实体 createTime/ts/loginAt 同语义。
 */
@Data
public class AdminLogQueryDTO {
    /** 页码，从 1 开始；null/<=0 兜底为 1。 */
    private Integer pageNum;
    /** 每页大小；null/<=0 兜底为 10。 */
    private Integer pageSize;
    /** 仅 oplog 用：按操作账号过滤；null=不限。 */
    private Long adminId;
    /** 仅 oplog 用：按权限码（perm_code）精确过滤；null=不限。 */
    private String permCode;
    /** 起始时间（毫秒戳，闭区间 >=）。 */
    private Long startTime;
    /** 截止时间（毫秒戳，闭区间 <=）。 */
    private Long endTime;
}
