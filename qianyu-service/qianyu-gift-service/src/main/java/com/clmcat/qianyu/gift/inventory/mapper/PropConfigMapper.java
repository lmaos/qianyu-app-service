package com.clmcat.qianyu.gift.inventory.mapper;

import com.clmcat.qianyu.gift.inventory.model.entity.PropConfig;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * prop_config 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Mapper
public interface PropConfigMapper extends BaseMapper<PropConfig> {

    /**
     * 按 ID 查询道具配置。
     */
    @Select("SELECT * FROM prop_config WHERE id = #{id}")
    PropConfig customSelectById(@Param("id") long id);

    /**
     * 查询所有上架道具（按类型分组）。
     */
    @Select("SELECT * FROM prop_config WHERE status = 1 ORDER BY prop_type, id")
    List<PropConfig> customSelectAllEnabled();
}
