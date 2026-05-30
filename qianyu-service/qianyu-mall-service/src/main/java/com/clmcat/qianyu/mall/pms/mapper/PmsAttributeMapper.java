package com.clmcat.qianyu.mall.pms.mapper;

import com.clmcat.qianyu.mall.pms.model.entity.PmsAttribute;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmsAttributeMapper extends BaseMapper<PmsAttribute> {

    /**
     * 按分类 ID 查询销售属性（type=1，用于生成规格组）
     */
    @Select("SELECT * FROM pms_attribute WHERE category_id = #{categoryId} AND type = 1 AND deleted = 0 ORDER BY sort ASC")
    List<PmsAttribute> selectSaleAttrByCategoryId(@Param("categoryId") Long categoryId);
}
