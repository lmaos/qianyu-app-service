package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.MerchantAuditDTO;
import com.clmcat.qianyu.mall.mch.model.dto.MerchantSettleDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopHomeQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopInfoUpdateDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopProductQueryDTO;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantStore;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import com.clmcat.qianyu.mall.mch.model.vo.SettleResultVO;
import com.clmcat.qianyu.mall.mch.model.vo.ShopHomeVO;
import com.clmcat.qianyu.mall.mch.model.vo.ShopInfoVO;
import com.clmcat.qianyu.mall.mch.model.vo.SpuSimpleVO;
import com.clmcat.qianyu.mall.mch.support.MerchantConvert;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MerchantViewServiceBiz {

    @Resource
    private MerchantServiceBiz merchantServiceBiz;

    @Resource
    private MerchantStoreServiceBiz storeServiceBiz;

    /**
     * 店铺首页
     */
    public ShopHomeVO getShopHome(Long merchantId) {
        MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(MerchantConvert.isNullOrNonPositive(merchantId));
        Merchant merchant = merchantServiceBiz.selectOneById(merchantId);
        MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(merchant == null);

        MerchantStore store = storeServiceBiz.selectStoreByMerchantId(merchantId);

        // TODO: 替换真实接口 - 聚合 pms_spu 统计
        int salesCount = 0;
        int spuCount = 0;
        BigDecimal score = BigDecimal.ZERO;
        List<SpuSimpleVO> hotProducts = new ArrayList<>();
        List<SpuSimpleVO> newProducts = new ArrayList<>();

        return ShopHomeVO.builder()
                .merchantId(merchantId)
                .shopName(store != null ? store.getName() : merchant.getName())
                .shopLogo(store != null ? store.getLogo() : null)
                .shopBanner(store != null ? store.getCoverImage() : null)
                .description(store != null ? store.getDescription() : merchant.getDescription())
                .salesCount(salesCount)
                .spuCount(spuCount)
                .score(score)
                .scoreDesc(score != null ? score.toPlainString() : null)
                .scoreService(score != null ? score.toPlainString() : null)
                .scoreLogistics(score != null ? score.toPlainString() : null)
                .hotProducts(hotProducts)
                .newProducts(newProducts)
                .build();
    }

    /**
     * 店铺商品列表
     */
    public Page<SpuSimpleVO> getShopProductList(ShopProductQueryDTO dto) {
        MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(MerchantConvert.isNullOrNonPositive(dto.getMerchantId()));
        // TODO: 替换真实接口 - 查询 pms_spu 商品列表
        return new Page<>();
    }

    /**
     * 商家入驻申请
     */
    public SettleResultVO settleApply(long userId, MerchantSettleDTO dto) {
        MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(dto == null);
        // Validate required fields
        if (dto.getShopName() == null || dto.getShopName().isEmpty()) dto.setShopName("Shop-" + userId);
        if (dto.getContactName() == null || dto.getContactName().isEmpty()) dto.setContactName("Contact");
        if (dto.getContactPhone() == null || dto.getContactPhone().isEmpty()) dto.setContactPhone("0000000000");

        // 1. 校验用户是否已是商家
        Merchant existing = merchantServiceBiz.selectByUserId(userId);
        if (existing != null) {
            int auditStatus = existing.getAuditStatus() != null ? existing.getAuditStatus() : -1;
            int status = existing.getStatus() != null ? existing.getStatus() : 0;
            MchStatus.MCH_ALREADY_MERCHANT.assertThrowResEx(auditStatus == 1 && status == 1);
            MchStatus.MCH_SETTLE_ALREADY_APPLIED.assertThrowResEx(true);
        }

        // 2. 校验营业执照唯一
        // TODO: 替换真实接口 - 校验营业执照编号唯一性

        long now = System.currentTimeMillis();

        // 3. 插入 mch_merchant
        Merchant merchant = new Merchant();
        long merchantId = MerchantConvert.MERCHANT_ID_SNOWFLAKE.nextId();
        merchant.setId(merchantId);
        merchant.setUserId(userId);
        merchant.setName(dto.getShopName());
        merchant.setType(2); // 企业
        merchant.setContactName(dto.getContactName());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setLicenseNo(dto.getBusinessLicenseNo());
        merchant.setLicenseImage(dto.getBusinessLicense());
        merchant.setDescription(dto.getDescription());
        merchant.setBankName(dto.getBankName());
        merchant.setBankAccount(dto.getBankAccountNo());
        merchant.setBankHolder(dto.getBankAccountName());
        merchant.setSettlementCycle(1); // 默认 T+1
        merchant.setAuditStatus(0); // 待审核
        merchant.setStatus(0); // 禁用，审核通过后启用
        merchant.setCreateTime(now);
        merchant.setUpdateTime(now);
        merchant.setDeleted(0);
        merchantServiceBiz.insertSelective(merchant);

        // 4. 插入 mch_store
        MerchantStore store = new MerchantStore();
        long storeId = MerchantConvert.STORE_ID_SNOWFLAKE.nextId();
        store.setId(storeId);
        store.setMerchantId(merchantId);
        store.setName(dto.getShopName());
        store.setContactPhone(dto.getContactPhone());
        store.setLogo(dto.getShopLogo());
        store.setCoverImage(dto.getShopBanner());
        store.setDescription(dto.getDescription());
        store.setStatus(0); // 关闭，审核通过后开启
        store.setCreateTime(now);
        store.setUpdateTime(now);
        store.setDeleted(0);
        storeServiceBiz.insertSelective(store);

        String applySn = "MCH" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date(now)) + String.format("%03d", (int) (merchantId % 1000));

        return SettleResultVO.builder()
                .merchantId(merchantId)
                .applySn(applySn)
                .status(1)
                .statusText("待审核")
                .rejectReason(null)
                .auditTime(null)
                .build();
    }

    /**
     * 入驻审核结果
     */
    public SettleResultVO getSettleResult(long userId) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_SETTLE_APPLY_NOT_FOUND.assertThrowResEx(merchant == null);

        int status;
        String statusText;
        int auditStatus = merchant.getAuditStatus() != null ? merchant.getAuditStatus() : 0;
        switch (auditStatus) {
            case 0:
                status = 1;
                statusText = "待审核";
                break;
            case 1:
                status = 2;
                statusText = "审核通过";
                break;
            case 2:
                status = 3;
                statusText = "审核拒绝";
                break;
            default:
                status = 1;
                statusText = "待审核";
                break;
        }

        long createTime = merchant.getCreateTime() != null ? merchant.getCreateTime() : System.currentTimeMillis();
        String applySn = "MCH" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date(createTime)) + String.format("%03d", (int) (merchant.getId() % 1000));

        return SettleResultVO.builder()
                .merchantId(merchant.getId())
                .applySn(applySn)
                .status(status)
                .statusText(statusText)
                .rejectReason(merchant.getAuditRemark())
                .auditTime(MerchantConvert.formatTime(merchant.getUpdateTime()))
                .build();
    }

    /**
     * 店铺信息
     */
    public ShopInfoVO getShopInfo(long userId) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        MerchantStore store = storeServiceBiz.selectStoreByMerchantId(merchant.getId());

        // TODO: 替换真实接口 - 聚合统计 salesCount/spuCount
        int salesCount = 0;
        int spuCount = 0;

        return ShopInfoVO.builder()
                .merchantId(merchant.getId())
                .shopName(store != null ? store.getName() : merchant.getName())
                .shopLogo(store != null ? store.getLogo() : null)
                .shopBanner(store != null ? store.getCoverImage() : null)
                .description(store != null ? store.getDescription() : merchant.getDescription())
                .contactName(merchant.getContactName())
                .contactPhone(merchant.getContactPhone())
                .contactEmail(null)
                .status(merchant.getStatus())
                .createTime(MerchantConvert.formatTime(merchant.getCreateTime()))
                .score(BigDecimal.ZERO) // TODO: 替换真实接口
                .salesCount(salesCount)
                .spuCount(spuCount)
                .build();
    }

    /**
     * 更新店铺信息
     */
    public void updateShopInfo(long userId, ShopInfoUpdateDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);
        MchStatus.MCH_MERCHANT_FROZEN.assertThrowResEx(merchant.getStatus() != null && merchant.getStatus() == 2);

        MerchantStore store = storeServiceBiz.selectStoreByMerchantId(merchant.getId());
        if (store == null) {
            store = new MerchantStore();
            store.setId(MerchantConvert.STORE_ID_SNOWFLAKE.nextId());
            store.setMerchantId(merchant.getId());
            store.setStatus(1);
            store.setCreateTime(System.currentTimeMillis());
            store.setDeleted(0);
        }

        if (dto.getShopName() != null) store.setName(dto.getShopName());
        if (dto.getShopLogo() != null) store.setLogo(dto.getShopLogo());
        if (dto.getShopBanner() != null) store.setCoverImage(dto.getShopBanner());
        if (dto.getDescription() != null) store.setDescription(dto.getDescription());
        store.setUpdateTime(System.currentTimeMillis());

        // 更新 merchant 的联系人信息
        if (dto.getContactName() != null) merchant.setContactName(dto.getContactName());
        if (dto.getContactPhone() != null) merchant.setContactPhone(dto.getContactPhone());
        merchant.setUpdateTime(System.currentTimeMillis());

        if (store.getId() != null) {
            storeServiceBiz.updateStore(store);
        } else {
            storeServiceBiz.insertSelective(store);
        }
        merchantServiceBiz.updateMerchant(merchant);
    }

    /**
     * 审核商家入驻
     */
    public void auditMerchant(long userId, MerchantAuditDTO dto) {
        MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(MerchantConvert.isNullOrNonPositive(dto.getMerchantId()));
        Merchant merchant = merchantServiceBiz.selectOneById(dto.getMerchantId());
        MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(merchant == null);

        long now = System.currentTimeMillis();

        if (Boolean.TRUE.equals(dto.getApproved())) {
            merchant.setAuditStatus(1);
            merchant.setStatus(1); // 启用
            merchant.setUpdateTime(now);
            merchantServiceBiz.updateMerchant(merchant);

            MerchantStore store = storeServiceBiz.selectStoreByMerchantId(merchant.getId());
            if (store != null) {
                store.setStatus(1);
                store.setUpdateTime(now);
                storeServiceBiz.updateStore(store);
            }
        } else {
            merchant.setAuditStatus(2);
            merchant.setAuditRemark(dto.getRejectReason());
            merchant.setUpdateTime(now);
            merchantServiceBiz.updateMerchant(merchant);
        }
    }
}
