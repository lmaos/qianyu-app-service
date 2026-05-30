package com.clmcat.qianyu.mall.pms.mapper;

import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsCategoryTableDef.PMS_CATEGORY;

@Mapper
public interface PmsCategoryMapper extends BaseMapper<PmsCategory> {

    /**
     * 查询所有启用的分类（status=0），按 sort ASC 排序
     * 注意：deleted=0 条件由 MyBatis-Flex 逻辑删除自动追加，不需要手动写
     */
    default List<PmsCategory> selectAllEnabled() {
        return selectListByQuery(
            com.mybatisflex.core.query.QueryWrapper.create()
                .where(PMS_CATEGORY.STATUS.eq(0))
                .orderBy(PMS_CATEGORY.SORT.asc())
        );
    }

    /**
     * 查询子分类数量
     */
    @Select("SELECT COUNT(*) FROM pms_category WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 查询分类下商品数量（通过 pms_spu.category_id）
     */
    @Select("SELECT COUNT(*) FROM pms_spu WHERE category_id = #{categoryId} AND deleted = 0")
    int countProducts(@Param("categoryId") Long categoryId);

    /**
     * 查询分类下商品数量（通过 pms_spu_category 关联表）
     */
    @Select("SELECT COUNT(*) FROM pms_spu_category WHERE category_id = #{categoryId}")
    int countProductsByRelation(@Param("categoryId") Long categoryId);

    /**
     * 检查同级下分类名是否重复
     */
    @Select("SELECT COUNT(*) FROM pms_category WHERE name = #{name} AND parent_id = #{parentId} AND deleted = 0")
    int countByNameAndParent(@Param("name") String name, @Param("parentId") Long parentId);
}
