package com.clmcat.qianyu.mall.msg.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 通知展示项。 */
@Data
@Builder
@Schema(description = "通知展示项")
public class MsgVO {
    @Schema(description = "通知ID")      private Long messageId;
    @Schema(description = "类型")        private Integer type;
    @Schema(description = "标题")        private String title;
    @Schema(description = "正文")        private String content;
    @Schema(description = "业务类型(跳转用)") private String bizType;
    @Schema(description = "业务ID(跳转用)")   private Long bizId;
    @Schema(description = "是否已读 0/1") private Integer isRead;
    @Schema(description = "创建时间(毫秒戳)") private Long createTime;
    @Schema(description = "创建时间(yyyy-MM-dd HH:mm:ss)") private String createTimeText;
}
