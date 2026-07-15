package com.clmcat.qianyu.mall.msg.service;

import com.clmcat.qianyu.mall.msg.model.dto.MsgIdDTO;
import com.clmcat.qianyu.mall.msg.model.dto.MsgListDTO;
import com.clmcat.qianyu.mall.msg.model.dto.MsgTypeDTO;
import com.clmcat.qianyu.mall.msg.model.vo.MsgCountVO;
import com.clmcat.qianyu.mall.msg.model.vo.MsgVO;
import com.mybatisflex.core.paginate.Page;

/**
 * 系统通知 C 端业务接口（当前登录用户视角）。
 * <p>由 {@code MsgController}（/api/mall/msg/*，{@code @LoginVerify}）调用。
 */
public interface MsgViewServiceBiz {

    /** 通知列表（分页，可选 type / 仅未读）。 */
    Page<MsgVO> list(long userId, MsgListDTO dto);

    /** 未读数（红点）。 */
    MsgCountVO unreadCount(long userId, MsgTypeDTO dto);

    /** 标记单条已读。 */
    void read(long userId, MsgIdDTO dto);

    /** 全部已读（可按 type）。 */
    void readAll(long userId, MsgTypeDTO dto);
}
