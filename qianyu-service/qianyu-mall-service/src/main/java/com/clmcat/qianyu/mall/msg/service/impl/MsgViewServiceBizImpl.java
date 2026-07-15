package com.clmcat.qianyu.mall.msg.service.impl;

import com.clmcat.qianyu.mall.msg.model.dto.MsgIdDTO;
import com.clmcat.qianyu.mall.msg.model.dto.MsgListDTO;
import com.clmcat.qianyu.mall.msg.model.dto.MsgTypeDTO;
import com.clmcat.qianyu.mall.msg.model.entity.MsgMessage;
import com.clmcat.qianyu.mall.msg.model.entity.status.MsgStatus;
import com.clmcat.qianyu.mall.msg.model.vo.MsgCountVO;
import com.clmcat.qianyu.mall.msg.model.vo.MsgVO;
import com.clmcat.qianyu.mall.msg.rpc.MsgApiImpl;
import com.clmcat.qianyu.mall.msg.service.MsgViewServiceBiz;
import com.clmcat.qianyu.mall.msg.support.MsgConvert;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统通知 C 端业务实现。{@code @Resource MsgApiImpl} 同进程直调（照 fav 范式）。
 */
@Service
public class MsgViewServiceBizImpl implements MsgViewServiceBiz {

    @Resource
    private MsgApiImpl msgApiImpl;

    @Override
    public Page<MsgVO> list(long userId, MsgListDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();
        Integer type = dto == null ? null : dto.getType();
        Boolean onlyUnread = dto == null ? null : dto.getOnlyUnread();

        Page<MsgMessage> page = msgApiImpl.pageByUser(userId, type, onlyUnread, pageNum, pageSize);
        List<MsgMessage> records = page.getRecords() == null ? List.of() : page.getRecords();
        List<MsgVO> voList = records.stream().map(MsgConvert::toVO).collect(Collectors.toList());

        Page<MsgVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotalRow(page.getTotalRow());
        return voPage;
    }

    @Override
    public MsgCountVO unreadCount(long userId, MsgTypeDTO dto) {
        Integer type = dto == null ? null : dto.getType();
        return MsgCountVO.builder().count(msgApiImpl.countUnread(userId, type)).build();
    }

    @Override
    public void read(long userId, MsgIdDTO dto) {
        MsgStatus.MSG_ID_INVALID.assertThrowResEx(
                dto == null || dto.getMessageId() == null || dto.getMessageId() <= 0);
        msgApiImpl.markRead(userId, dto.getMessageId()); // 不存在/非本人/已读 → affected=0，幂等无异常
    }

    @Override
    public void readAll(long userId, MsgTypeDTO dto) {
        Integer type = dto == null ? null : dto.getType();
        msgApiImpl.markAllRead(userId, type);
    }
}
