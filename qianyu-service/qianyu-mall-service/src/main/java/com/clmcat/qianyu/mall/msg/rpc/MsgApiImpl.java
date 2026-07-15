package com.clmcat.qianyu.mall.msg.rpc;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.api.msg.MsgApi;
import com.clmcat.qianyu.mall.msg.mapper.MsgMessageMapper;
import com.clmcat.qianyu.mall.msg.model.entity.MsgMessage;
import com.clmcat.qianyu.mall.msg.model.entity.status.MsgStatus;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * 系统通知投递 + 查询/更新实现。
 * <p>{@code @DubboService}（跨模块 {@code send} 经 Dubbo）；同模块 C 端 {@code MsgViewServiceBiz} 经
 * {@code @Resource} 直调 {@code pageByUser/countUnread/markRead/markAllRead}（照 fav 范式）。
 */
@Slf4j
@DubboService
@Service
public class MsgApiImpl implements MsgApi {

    /** 通知雪花 ID（workerId 与其它模块错开由基准决定；此处用项目统一基准 42/10/11）。 */
    private static final CustomSnowflake MSG_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    @Resource
    private MsgMessageMapper msgMapper;

    // ==================== Dubbo 契约：跨模块投递 ====================

    @Override
    public long send(Long userId, Integer type, String title, String content, String bizType, Long bizId) {
        MsgStatus.MSG_SEND_PARAM_INVALID.assertThrowResEx(
                userId == null || userId <= 0 || type == null || title == null || title.isEmpty());
        long now = System.currentTimeMillis();
        MsgMessage m = new MsgMessage();
        m.setId(MSG_ID_SNOWFLAKE.nextId());
        m.setUserId(userId);
        m.setType(type);
        m.setTitle(title);
        m.setContent(content == null ? "" : content);
        m.setBizType(bizType == null ? "" : bizType);
        m.setBizId(bizId == null ? 0L : bizId);
        m.setIsRead(0);
        m.setReadTime(0L);
        m.setCreateTime(now);
        m.setUpdateTime(now);
        m.setDeleted(0);
        msgMapper.insert(m);
        log.info("投递通知 userId={} type={} bizType={} bizId={} msgId={}", userId, type, bizType, bizId, m.getId());
        return m.getId();
    }

    // ==================== 同模块内部方法（ViewBiz 直调） ====================

    /** 分页查用户通知（可选 type / onlyUnread），按创建时间倒序。 */
    public Page<MsgMessage> pageByUser(Long userId, Integer type, Boolean onlyUnread, int pageNum, int pageSize) {
        QueryWrapper qw = QueryWrapper.create().where("user_id = ?", userId).and("deleted = ?", 0);
        if (type != null && type > 0) {
            qw.and("type = ?", type);
        }
        if (Boolean.TRUE.equals(onlyUnread)) {
            qw.and("is_read = ?", 0);
        }
        qw.orderBy("create_time DESC");
        return msgMapper.paginate(Page.of(pageNum, pageSize), qw);
    }

    public long countUnread(Long userId, Integer type) {
        return msgMapper.countUnread(userId, type);
    }

    /**
     * 单条已读（CAS：is_read 0→1；含 user_id 越权防护，只能标自己的）。
     * @return affected（0=不存在/非本人/已读，幂等无异常）
     */
    public int markRead(Long userId, Long messageId) {
        long now = System.currentTimeMillis();
        MsgMessage up = new MsgMessage();
        up.setIsRead(1);
        up.setReadTime(now);
        up.setUpdateTime(now);
        return msgMapper.updateByQuery(up,
                QueryWrapper.create().where("id = ?", messageId)
                        .and("user_id = ?", userId)
                        .and("is_read = ?", 0));
    }

    /** 全部已读（可选 type）。@return affected 行数。 */
    public int markAllRead(Long userId, Integer type) {
        long now = System.currentTimeMillis();
        MsgMessage up = new MsgMessage();
        up.setIsRead(1);
        up.setReadTime(now);
        up.setUpdateTime(now);
        QueryWrapper qw = QueryWrapper.create()
                .where("user_id = ?", userId).and("is_read = ?", 0).and("deleted = ?", 0);
        if (type != null && type > 0) {
            qw.and("type = ?", type);
        }
        return msgMapper.updateByQuery(up, qw);
    }
}
