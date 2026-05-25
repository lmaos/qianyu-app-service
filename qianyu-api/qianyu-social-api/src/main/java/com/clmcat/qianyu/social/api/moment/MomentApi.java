package com.clmcat.qianyu.social.api.moment;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;

import java.util.List;

public interface MomentApi {
    /**
     * 保存 moment
     * @param moment
     */
    boolean save(MomentDto moment);

    MomentDto getMomentById(long id);

    List<MomentDto> getMomentByIds(List<Long> ids);

    List<MomentDto> getMomentByAuthorId(String authorId, long nextMomentId, int limit);

    /**
     * 通过作者编号查询动态，按 momentId 倒序游标分页。
     *
     * @param authorId 作者编号
     * @param nextMomentId 下一页游标，查询 momentId 小于该值的数据
     * @param limit 查询条数
     * @return 动态列表
     */
    List<MomentDto> getMomentByAuthorId(long authorId, long nextMomentId, int limit);

    List<Long> getMomentIdsByAuthorId(long authorId, long nextMomentId, int limit);

    boolean deleteMomentById(long moment);

    boolean deleteMomentByIdAndAuthorId(long momentId, long authorId);
}
