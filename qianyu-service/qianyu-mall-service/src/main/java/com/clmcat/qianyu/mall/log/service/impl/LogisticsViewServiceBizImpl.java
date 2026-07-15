package com.clmcat.qianyu.mall.log.service.impl;

import com.clmcat.qianyu.mall.log.rpc.DeliveryTraceApiImpl;
import com.clmcat.qianyu.mall.log.rpc.LogisticsApiImpl;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsCreateDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsListQueryDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsPushDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsQueryDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsUpdateDTO;
import com.clmcat.qianyu.mall.log.model.entity.LogDeliveryTrace;
import com.clmcat.qianyu.mall.log.model.entity.LogShipping;
import com.clmcat.qianyu.mall.log.model.entity.status.LogisticsStatus;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsDetailVO;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsListItemVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.clmcat.qianyu.mall.log.support.LogisticsConvert;
import com.clmcat.qianyu.mall.log.tracker.LogisticsTracker;
import com.clmcat.qianyu.mall.log.tracker.LogisticsTrackerFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import com.clmcat.qianyu.mall.log.service.LogisticsViewServiceBiz;

@Slf4j
@Service
public class LogisticsViewServiceBizImpl implements LogisticsViewServiceBiz {

    @Resource
    private LogisticsApiImpl logisticsServiceBiz;

    @Resource
    private DeliveryTraceApiImpl traceServiceBiz;

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    @Resource
    private LogisticsTrackerFactory trackerFactory;

    /**
     * 根据订单 ID 查询物流
     */
    public LogisticsDetailVO queryByOrderId(long userId, LogisticsQueryDTO dto) {
        Long orderId = dto == null ? null : dto.getOrderId();
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(LogisticsConvert.isNullOrNonPositive(orderId));

        List<LogShipping> shippings = logisticsServiceBiz.selectByOrderId(orderId);
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(shippings == null || shippings.isEmpty());

        LogShipping shipping = shippings.get(0);
        List<LogDeliveryTrace> traces = traceServiceBiz.selectByShippingId(shipping.getId());
        return LogisticsConvert.toDetailVO(shipping, traces);
    }

    /**
     * 实时查询物流轨迹
     */
    public LogisticsDetailVO trackRealtime(long userId, LogisticsQueryDTO dto) {
        Long logisticsId = dto == null ? null : dto.getLogisticsId();
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(LogisticsConvert.isNullOrNonPositive(logisticsId));

        LogShipping shipping = logisticsServiceBiz.selectOneById(logisticsId);
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(shipping == null);

        // M5: 调用物流轨迹提供商（Mock/快递鸟/快递100，由 config 决定）
        LogisticsTracker tracker = trackerFactory.getTracker();
        List<LogisticsTracker.TracePoint> remoteTraces = tracker.track(
                shipping.getShippingCompany(), shipping.getShippingNo());
        // 合并远程轨迹到本地缓存
        long now = System.currentTimeMillis();
        for (LogisticsTracker.TracePoint tp : remoteTraces) {
            LogDeliveryTrace trace = new LogDeliveryTrace();
            trace.setId(LogisticsConvert.LOG_ID_SNOWFLAKE.nextId());
            trace.setShippingId(shipping.getId());
            trace.setDescription(tp.getDescription());
            trace.setLocation(tp.getLocation());
            trace.setSource(1); // 第三方API
            trace.setCarrierCode(shipping.getShippingCompany());
            trace.setCreateTime(now);
            traceServiceBiz.insertTrace(trace);
        }
        // 返回合并后的全量轨迹（含本地缓存 + 远程拉取）
        List<LogDeliveryTrace> traces = traceServiceBiz.selectByShippingId(shipping.getId());

        // 如果已签收，触发 OMS 订单自动确认收货
        if (shipping.getStatus() != null && shipping.getStatus() == 2) {
            // TODO：替换真实接口 - 触发 OMS 订单自动确认收货
        }

        return LogisticsConvert.toDetailVO(shipping, traces);
    }

    /**
     * 创建物流单
     */
    public Long createLogistics(long userId, LogisticsCreateDTO dto) {
        LogisticsStatus.LOG_LOGISTICS_NOT_BELONG_MERCHANT.assertThrowResEx(dto == null);
        LogisticsStatus.LOG_LOGISTICS_NOT_BELONG_MERCHANT.assertThrowResEx(LogisticsConvert.isNullOrNonPositive(dto.getOrderId()));

        // S17: 商家归属校验（HTTP 调用 userId>0；shipOrder 内部 userId=0 已外层校验订单归属，跳过）
        if (userId > 0) {
            MerchantDto m = merchantApi.requireActiveMerchant(userId);
            OmsOrderDto order = omsOrderApi.findById(dto.getOrderId());
            LogisticsStatus.LOG_LOGISTICS_NOT_BELONG_MERCHANT.assertThrowResEx(
                    m == null || order == null || !m.getId().equals(order.getMerchantId()));
        }

        // 检查是否已发货
        List<LogShipping> existing = logisticsServiceBiz.selectByOrderId(dto.getOrderId());
        LogisticsStatus.LOG_ORDER_ALREADY_SHIPPED.assertThrowResEx(existing != null && !existing.isEmpty());

        // 校验物流编码
        LogisticsStatus.LOG_LOGISTICS_CODE_INVALID.assertThrowResEx(dto.getLogisticsCode() == null || dto.getLogisticsCode().isEmpty());
        LogisticsStatus.LOG_LOGISTICS_NO_INVALID.assertThrowResEx(dto.getLogisticsNo() == null || dto.getLogisticsNo().isEmpty());

        long now = System.currentTimeMillis();
        LogShipping shipping = new LogShipping();
        shipping.setId(LogisticsConvert.LOG_ID_SNOWFLAKE.nextId());
        shipping.setOrderId(dto.getOrderId());
        shipping.setOrderItemId(dto.getOrderItemId());
        shipping.setShippingNo(dto.getLogisticsNo());
        shipping.setShippingCompany(dto.getLogisticsCode());
        shipping.setShippingCompanyName(dto.getLogisticsCompany());
        shipping.setStatus(0); // 已发货
        shipping.setDeliveryTime(now);
        shipping.setCreateTime(now);
        shipping.setUpdateTime(now);
        shipping.setDeleted(0);

        logisticsServiceBiz.insertSelective(shipping);
        return shipping.getId();
    }

    /**
     * 更新物流
     */
    public void updateLogistics(long userId, LogisticsUpdateDTO dto) {
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(dto == null);
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(LogisticsConvert.isNullOrNonPositive(dto.getLogisticsId()));

        LogShipping shipping = logisticsServiceBiz.selectOneById(dto.getLogisticsId());
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(shipping == null);

        // S17: 商家归属校验（HTTP 调用 userId>0）
        if (userId > 0) {
            MerchantDto m = merchantApi.requireActiveMerchant(userId);
            OmsOrderDto order = omsOrderApi.findById(shipping.getOrderId());
            LogisticsStatus.LOG_LOGISTICS_NOT_BELONG_MERCHANT.assertThrowResEx(
                    m == null || order == null || !m.getId().equals(order.getMerchantId()));
        }

        // 已签收不可修改
        LogisticsStatus.LOG_LOGISTICS_ALREADY_SIGNED.assertThrowResEx(
                shipping.getStatus() != null && shipping.getStatus() == 2);

        if (dto.getLogisticsCompany() != null) {
            shipping.setShippingCompanyName(dto.getLogisticsCompany());
        }
        if (dto.getLogisticsCode() != null) {
            shipping.setShippingCompany(dto.getLogisticsCode());
        }
        if (dto.getLogisticsNo() != null) {
            shipping.setShippingNo(dto.getLogisticsNo());
        }
        shipping.setUpdateTime(System.currentTimeMillis());
        logisticsServiceBiz.update(shipping);
    }

    /**
     * 处理物流公司推送
     */
    public Boolean handlePush(LogisticsPushDTO dto) {
        LogisticsStatus.LOG_SIGN_VERIFY_FAIL.assertThrowResEx(dto == null);
        // TODO：替换真实接口 - 验证签名
        // LogisticsStatus.LOG_SIGN_VERIFY_FAIL.assertThrowResEx(verifySignFailed);

        LogisticsStatus.LOG_LOGISTICS_CODE_INVALID.assertThrowResEx(
                dto.getLogisticsCode() == null || dto.getLogisticsNo() == null);

        LogShipping shipping = logisticsServiceBiz.selectByCodeAndNo(dto.getLogisticsCode(), dto.getLogisticsNo());
        LogisticsStatus.LOG_LOGISTICS_NOT_FOUND.assertThrowResEx(shipping == null);

        // 更新物流状态（API status -> SQL status）
        if (dto.getStatus() != null) {
            shipping.setStatus(dto.getStatus());
        }
        shipping.setUpdateTime(System.currentTimeMillis());

        // 如果已签收
        if (shipping.getStatus() != null && shipping.getStatus() == 2) {
            shipping.setReceiveTime(System.currentTimeMillis());
            // TODO：替换真实接口 - 触发 OMS 订单自动确认收货
        }
        logisticsServiceBiz.update(shipping);

        // 写入轨迹
        if (dto.getTraces() != null) {
            for (var traceDto : dto.getTraces()) {
                LogDeliveryTrace trace = new LogDeliveryTrace();
                trace.setId(LogisticsConvert.LOG_ID_SNOWFLAKE.nextId());
                trace.setShippingId(shipping.getId());
                trace.setDescription(traceDto.getContent());
                trace.setLocation(traceDto.getLocation());
                trace.setSource(1); // 第三方回调推送
                trace.setCarrierCode(dto.getLogisticsCode());
                trace.setCreateTime(System.currentTimeMillis());
                // TODO：替换真实接口 - traceTime 解析
                traceServiceBiz.insertTrace(trace);
            }
        }

        return true;
    }

    /**
     * B端商家物流列表
     */
    public Page<LogisticsListItemVO> logisticsList(long userId, LogisticsListQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create();
        if (dto != null && dto.getShippingNo() != null && !dto.getShippingNo().isEmpty()) {
            qw.where("shipping_no LIKE ?", "%" + dto.getShippingNo() + "%");
        }
        qw.orderBy("create_time DESC");

        Page<LogShipping> shippingPage = logisticsServiceBiz.paginate(Page.of(pageNum, pageSize), qw);
        if (shippingPage == null || shippingPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        java.util.List<LogisticsListItemVO> voList = new java.util.ArrayList<>();
        for (LogShipping s : shippingPage.getRecords()) {
            voList.add(LogisticsListItemVO.builder()
                    .id(s.getId())
                    .orderId(s.getOrderId())
                    .shippingNo(s.getShippingNo())
                    .shippingCompanyName(s.getShippingCompanyName())
                    .status(s.getStatus())
                    .statusText(shippingStatusText(s.getStatus()))
                    .deliveryTime(s.getDeliveryTime() != null ? String.valueOf(s.getDeliveryTime()) : null)
                    .receiveTime(s.getReceiveTime() != null ? String.valueOf(s.getReceiveTime()) : null)
                    .build());
        }

        Page<LogisticsListItemVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(shippingPage.getTotalRow());
        return result;
    }

    private String shippingStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "已发货";
            case 1 -> "运输中";
            case 2 -> "已签收";
            case 3 -> "异常";
            default -> "";
        };
    }
}
