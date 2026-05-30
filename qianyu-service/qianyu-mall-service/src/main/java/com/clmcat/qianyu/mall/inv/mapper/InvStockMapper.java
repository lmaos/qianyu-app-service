package com.clmcat.qianyu.mall.inv.mapper;

import com.clmcat.qianyu.mall.inv.model.entity.InvStock;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InvStockMapper extends BaseMapper<InvStock> {

    /**
     * 乐观锁扣减可用库存（锁定）
     * available_stock >= #{quantity} 条件防超卖
     */
    @Update("UPDATE inv_stock SET available_stock = available_stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}, " +
            "version = version + 1, update_time = #{updateTime} " +
            "WHERE sku_id = #{skuId} AND available_stock >= #{quantity} " +
            "AND version = #{version} AND deleted = 0")
    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity,
                  @Param("version") Long version, @Param("updateTime") Long updateTime);

    /**
     * 锁定转扣减（支付确认）
     */
    @Update("UPDATE inv_stock SET locked_stock = locked_stock - #{quantity}, " +
            "version = version + 1, update_time = #{updateTime} " +
            "WHERE sku_id = #{skuId} AND locked_stock >= #{quantity} " +
            "AND version = #{version} AND deleted = 0")
    int confirmStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity,
                     @Param("version") Long version, @Param("updateTime") Long updateTime);

    /**
     * 释放锁定回可用（取消/退款）
     */
    @Update("UPDATE inv_stock SET available_stock = available_stock + #{quantity}, " +
            "locked_stock = locked_stock - #{quantity}, " +
            "version = version + 1, update_time = #{updateTime} " +
            "WHERE sku_id = #{skuId} AND locked_stock >= #{quantity} " +
            "AND version = #{version} AND deleted = 0")
    int releaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity,
                     @Param("version") Long version, @Param("updateTime") Long updateTime);

    /**
     * 乐观锁调整可用库存（商家调整）
     */
    @Update("UPDATE inv_stock SET available_stock = available_stock + #{delta}, " +
            "version = version + 1, update_time = #{updateTime} " +
            "WHERE id = #{id} AND version = #{version} AND deleted = 0")
    int adjustStock(@Param("id") Long id, @Param("delta") int delta,
                    @Param("version") Long version, @Param("updateTime") long updateTime);
}
