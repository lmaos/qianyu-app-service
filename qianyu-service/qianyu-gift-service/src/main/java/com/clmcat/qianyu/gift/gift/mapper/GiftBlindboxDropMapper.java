package com.clmcat.qianyu.gift.gift.mapper;

import com.clmcat.qianyu.gift.gift.model.entity.GiftBlindboxDrop;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * gift_blindbox_drop 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Mapper
public interface GiftBlindboxDropMapper extends BaseMapper<GiftBlindboxDrop> {

    /**
     * 查询盲盒的启用掉落列表。
     */
    @Select("SELECT * FROM gift_blindbox_drop WHERE blindbox_gift_id = #{blindboxGiftId} AND status = 1")
    List<GiftBlindboxDrop> customSelectByBlindboxId(@Param("blindboxGiftId") long blindboxGiftId);
}
