package com.clmcat.qianyu.im.model.body;

import java.io.Serializable;

/**
 * 千语 IM 消息体
 *
 * 统一消息结构，用于：
 * 1. App → Server 发送请求的 body
 * 2. Server → IM 厂商转发的内容
 * 3. 本地存储的消息记录
 * 4. Server 返回给客户端的响应
 */
public class MessageBody implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客户端排序 ID
     * 格式: {10位秒级时间戳}{4位自增数}
     * 例: "17481600000001" */
    private String seqId;

    /** 客户端消息 ID
     * 格式: {sender}-{receiver}-{时间戳后8位}{4位自增数}
     * 例: "x451254-x4521245-800000010001" */
    private String msgId;

    /** 客户端时间（毫秒时间戳），服务器不做校验，透传存储 */
    private Long clientTime;

    /** 消息类型: text / image / voice */
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 发送者用户 ID */
    private String sender;

    /** 接收者用户 ID */
    private String receiver;

    /** 会话类型: 1-私聊 2-群聊 3-系统通知 */
    private Integer chatType;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }

    public Long getClientTime() { return clientTime; }
    public void setClientTime(Long clientTime) { this.clientTime = clientTime; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public Integer getChatType() { return chatType; }
    public void setChatType(Integer chatType) { this.chatType = chatType; }
}
