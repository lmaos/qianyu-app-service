package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.MerchantAuditDTO;
import com.clmcat.qianyu.mall.mch.model.dto.MerchantSettleDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopInfoUpdateDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopProductQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.StoreHomeQueryDTO;
import com.clmcat.qianyu.mall.mch.model.vo.MerchantDashboardVO;
import com.clmcat.qianyu.mall.mch.model.vo.SettleResultVO;
import com.clmcat.qianyu.mall.mch.model.vo.ShopHomeVO;
import com.clmcat.qianyu.mall.mch.model.vo.ShopInfoVO;
import com.clmcat.qianyu.mall.mch.model.vo.SpuSimpleVO;
import com.clmcat.qianyu.mall.mch.model.vo.StoreHomeVO;
import com.mybatisflex.core.paginate.Page;
import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuTableDef.PMS_SPU;

public interface MerchantViewServiceBiz {

    ShopHomeVO getShopHome(Long merchantId);

    StoreHomeVO getStoreHome(StoreHomeQueryDTO dto);

    Page<SpuSimpleVO> getShopProductList(ShopProductQueryDTO dto);

    SettleResultVO settleApply(long userId, MerchantSettleDTO dto);

    SettleResultVO getSettleResult(long userId);

    MerchantDashboardVO getDashboard(long userId);

    ShopInfoVO getShopInfo(long userId);

    void updateShopInfo(long userId, ShopInfoUpdateDTO dto);

    void auditMerchant(long userId, MerchantAuditDTO dto);

}