package com.clmcat.qianyu.im.model.vo;

import lombok.*;

/**
 * 消息发送校验结果
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageValidationResult {

    /** 是否通过校验 */
    private boolean valid;

    /** 校验结果描述 */
    private String message;
}
