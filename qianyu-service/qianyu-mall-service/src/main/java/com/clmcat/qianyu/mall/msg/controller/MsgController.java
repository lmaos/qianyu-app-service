package com.clmcat.qianyu.mall.msg.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.msg.model.dto.MsgIdDTO;
import com.clmcat.qianyu.mall.msg.model.dto.MsgListDTO;
import com.clmcat.qianyu.mall.msg.model.dto.MsgTypeDTO;
import com.clmcat.qianyu.mall.msg.model.vo.MsgCountVO;
import com.clmcat.qianyu.mall.msg.model.vo.MsgVO;
import com.clmcat.qianyu.mall.msg.service.MsgViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 消息中心（系统通知/站内信）。
 * <p>类级 {@code @LoginVerify}，方法经 {@code @Token} 注入当前 userId；统一信封自动包装。
 */
@Tag(name = "消息中心", description = "系统通知(站内信)拉取/已读")
@ApiController
@RequestMapping("/api/mall/msg")
@LoginVerify
public class MsgController {

    @Resource
    private MsgViewServiceBiz msgViewServiceBiz;

    @Operation(summary = "通知列表（分页，可选 type/仅未读）")
    @PostMapping("/list")
    public Page<MsgVO> list(@Parameter(hidden = true) @Token long userId, @Params MsgListDTO dto) {
        return msgViewServiceBiz.list(userId, dto);
    }

    @Operation(summary = "未读数（红点）")
    @PostMapping("/unreadCount")
    public MsgCountVO unreadCount(@Parameter(hidden = true) @Token long userId, @Params MsgTypeDTO dto) {
        return msgViewServiceBiz.unreadCount(userId, dto);
    }

    @Operation(summary = "标记单条已读")
    @PostMapping("/read")
    public void read(@Parameter(hidden = true) @Token long userId, @Params MsgIdDTO dto) {
        msgViewServiceBiz.read(userId, dto);
    }

    @Operation(summary = "全部已读（可按 type）")
    @PostMapping("/readAll")
    public void readAll(@Parameter(hidden = true) @Token long userId, @Params MsgTypeDTO dto) {
        msgViewServiceBiz.readAll(userId, dto);
    }
}
