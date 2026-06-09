package com.clmcat.qianyu.social.api.moment;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentIdListDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentListDto;

import java.util.List;

public interface MomentApi {
    /**
     * 保存 moment
     * @param moment
     */
    boolean save(MomentDto moment);

    MomentDto getMomentById(long id);

    MomentListDto getMomentByIds(List<Long> ids);

    /**
     * 通过作者编号查询动态，按 momentId 倒序游标分页。
     *
     * @param authorId 作者编号
     * @param nextMomentId 下一页游标，查询 momentId 小于该值的数据
     * @param limit 查询条数
     * @return 动态列表
     */
    MomentListDto getMomentByAuthorId(long authorId, long nextMomentId, int limit);

    MomentIdListDto getMomentIdsByAuthorId(long authorId, long nextMomentId, int limit);

    boolean deleteMomentById(long moment);

    boolean deleteMomentByIdAndAuthorId(long momentId, long authorId);
}
