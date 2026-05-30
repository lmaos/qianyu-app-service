package com.clmcat.qianyu.mall.pms.mapper;

import com.clmcat.qianyu.mall.pms.model.entity.PmsSpuCategory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmsSpuCategoryMapper extends BaseMapper<PmsSpuCategory> {

    /**
     * 按 SPU ID 删除所有关联
     */
    @Delete("DELETE FROM pms_spu_category WHERE spu_id = #{spuId}")
    int deleteBySpuId(@Param("spuId") Long spuId);

    /**
     * 批量插入 SPU-分类关联
     */
    @Insert("<script>" +
            "INSERT INTO pms_spu_category (id, spu_id, category_id, create_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "  (#{item.id}, #{item.spuId}, #{item.categoryId}, #{item.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<PmsSpuCategory> list);
}
