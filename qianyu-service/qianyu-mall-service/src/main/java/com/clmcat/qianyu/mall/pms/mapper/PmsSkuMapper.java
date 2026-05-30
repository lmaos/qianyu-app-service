package com.clmcat.qianyu.mall.pms.mapper;

import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PmsSkuMapper extends BaseMapper<PmsSku> {

    /**
     * 按 SPU ID 查询所有 SKU（未删除）
     */
    @Select("SELECT * FROM pms_sku WHERE spu_id = #{spuId} AND deleted = 0 ORDER BY is_default DESC, id ASC")
    List<PmsSku> selectBySpuId(@Param("spuId") Long spuId);

    /**
     * 查询 SPU 下默认 SKU
     */
    @Select("SELECT * FROM pms_sku WHERE spu_id = #{spuId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    PmsSku selectDefaultBySpuId(@Param("spuId") Long spuId);

    /**
     * 清除 SPU 下所有 SKU 的默认标记（用于设置新默认 SKU 前置操作）
     */
    @Update("UPDATE pms_sku SET is_default = 0, update_time = #{updateTime} " +
            "WHERE spu_id = #{spuId} AND is_default = 1 AND deleted = 0")
    int clearDefault(@Param("spuId") Long spuId, @Param("updateTime") Long updateTime);
}
