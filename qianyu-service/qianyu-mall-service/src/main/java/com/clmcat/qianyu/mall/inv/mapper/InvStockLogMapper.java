package com.clmcat.qianyu.mall.inv.mapper;

import com.clmcat.qianyu.mall.inv.model.entity.InvStockLog;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InvStockLogMapper extends BaseMapper<InvStockLog> {

    /**
     * 按 SPU ID 查询库存日志（JOIN pms_sku 获取 spuId）
     */
    @Select("<script>" +
            "SELECT l.* FROM inv_stock_log l " +
            "INNER JOIN pms_sku s ON l.sku_id = s.id " +
            "WHERE s.spu_id = #{spuId} " +
            "<if test='type != null'> AND l.type = #{type} </if> " +
            "<if test='startTime != null'> AND l.create_time &gt;= #{startTime} </if> " +
            "<if test='endTime != null'> AND l.create_time &lt;= #{endTime} </if> " +
            "ORDER BY l.create_time DESC" +
            "</script>")
    Page<InvStockLog> selectBySpuId(Page<InvStockLog> page,
                                      @Param("spuId") Long spuId,
                                      @Param("type") Integer type,
                                      @Param("startTime") Long startTime,
                                      @Param("endTime") Long endTime);
}
