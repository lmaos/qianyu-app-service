package com.clmcat.qianyu.im.model.body;

import java.io.Serializable;

/**
 * IM 发送请求体
 * App → Server 的完整请求结构
 * Server 读取 channel 路由到对应 IM 厂商
 */
public class SendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 路由标识: tencent / easemob / rongcloud / nim */
    private String channel;

    /** 消息体 */
    private MessageBody body;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public MessageBody getBody() { return body; }
    public void setBody(MessageBody body) { this.body = body; }
}
