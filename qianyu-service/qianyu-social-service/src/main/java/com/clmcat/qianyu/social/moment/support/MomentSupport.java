package com.clmcat.qianyu.social.moment.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdsDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentPublishDto;
import com.clmcat.qianyu.social.moment.model.entity.Moment;
import com.clmcat.qianyu.social.moment.model.vo.MomentVo;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MomentSupport {
    public static final int DEFAULT_AUTHOR_MOMENT_LIMIT = 20;
    public static final int MAX_AUTHOR_MOMENT_LIMIT = 100;

    public static final Set<String> MOMENT_TYPE_LIST = Set.of("text", "image", "video");

    /**
     * Moment ID 雪花生成器
      - 42 位时间戳（毫秒）+ 10 位机器ID + 11 位序列号
      - 每毫秒每台机器最多生成 2048 个 ID，足够高并发使用
     */
    public static final CustomSnowflake MOMENT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    // 全局 Geometry 工厂（创建经纬度 Point）
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326); // 4326 = GPS 坐标标准

    /**
     * 创建Moment， 并设置momentId
     */
    public static Moment newMoment(MomentDto dto) {
        if (dto == null) {
            return null;
        }
        long momentId = MOMENT_ID_SNOWFLAKE.nextId(); // 雪花ID
        long createTime = SnowflakeSupport.parseTimeBySnowflake(MOMENT_ID_SNOWFLAKE, momentId); // 解析时间戳，确保生成的 ID 是有效的
        dto.setMomentId(momentId);
        dto.setCreateTime(createTime);
        return toMoment(dto);
    }

    public static MomentDto toMomentDto(long authorId, MomentPublishDto dto) {
        if (dto == null) {
            return null;
        }

        MomentDto momentDto = new MomentDto();
        momentDto.setAuthorId(authorId);
        momentDto.setContent(dto.getContent());
        momentDto.setLatitude(dto.getLatitude());
        momentDto.setLongitude(dto.getLongitude());
        momentDto.setCountry(dto.getCountry());
        momentDto.setStatus(dto.getStatus());
        return momentDto;
    }

    /**
     * 将 MomentDto 转换为 Moment 实体类
      - 1. ID + 基础字段直接映射
      - 2. 经纬度 → 数据库 Point 类型
      - 3. 计数字段默认 0（新增时）
      - 4. 类型字段：根据内容类型设置，默认为 "text"
      - 5. 其他字段根据需要进行转换和映射
      - 6. 注意：MomentDto 中的经纬度是分开的 double 字段，而 Moment 实体类中是一个 Point 类型，需要进行转换
     */
    public static Moment toMoment(MomentDto dto) {
        if (dto == null) {
            return null;
        }

        Moment moment = new Moment();
        // 1. ID + 基础字段
        moment.setMomentId(dto.getMomentId());
        moment.setAuthorId(dto.getAuthorId());
        moment.setStatus(dto.getStatus());
        moment.setCountry(dto.getCountry());
        moment.setContent(dto.getContent());
        moment.setCreateTime(dto.getCreateTime());
        // 2. 经纬度 → 数据库 Point 类型
        // 注意：Point 顺序是 (经度 longitude, 纬度 latitude)
        if (dto.getLongitude() != 0.0 && dto.getLatitude() != 0.0) {
            Point point = GEOMETRY_FACTORY.createPoint(
                    new Coordinate(dto.getLongitude(), dto.getLatitude())
            );
            moment.setLocation(point);
        }
        // 4. 计数字段默认 0（新增时）
        moment.setLikes(0L);
        moment.setComments(0L);
        moment.setShares(0L);
        // 5. 类型字段
        String momentType = "text";
        if (dto.getContent() != null) {
            momentType = dto.getContent().getType();
            momentType = StringUtils.isBlank(momentType) ? "text" : momentType;
        }
        moment.setMomentType(momentType);

        return moment;
    }

    public static List<MomentDto> toMomentDtoList(Collection<Moment> moments) {
        List<MomentDto> momentDtoList = new ArrayList<>();
        for (Moment moment : moments) {
            momentDtoList.add(MomentSupport.toMomentDto(moment));
        }
        return momentDtoList;
    }

    public static MomentVo toMomentVo(MomentDto dto) {
        if (dto == null) {
            return null;
        }
        return MomentVo.builder()
                .momentId(dto.getMomentId())
                .authorId(dto.getAuthorId())
                .content(dto.getContent())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .country(dto.getCountry())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .build();
    }

    public static List<MomentVo> toMomentVoList(Collection<MomentDto> dtos) {
        List<MomentVo> momentVoList = new ArrayList<>();
        if (dtos == null) {
            return momentVoList;
        }
        for (MomentDto dto : dtos) {
            MomentVo momentVo = toMomentVo(dto);
            if (momentVo != null) {
                momentVoList.add(momentVo);
            }
        }
        return momentVoList;
    }

    public static MomentDto toMomentDto(Moment moment) {
        if (moment == null) {
            return null;
        }

        MomentDto dto = new MomentDto();

        // 1. 基础字段赋值
        dto.setMomentId(moment.getMomentId());
        dto.setAuthorId(moment.getAuthorId());
        dto.setContent(moment.getContent());
        dto.setStatus(moment.getStatus());
        dto.setCountry(moment.getCountry());
        dto.setCreateTime(moment.getCreateTime());

        // 2. 经纬度解析（从 Point 中拆出 longitude、latitude）
        Point location = moment.getLocation();
        if (location != null) {
            dto.setLongitude(location.getX()); // 经度 = X
            dto.setLatitude(location.getY());  // 纬度 = Y
        }

        return dto;
    }

    public static List<Long> normalizeMomentIds(MomentIdsDto dto) {
        LinkedHashSet<Long> momentIds = new LinkedHashSet<>();
        if (dto == null) {
            return new ArrayList<>();
        }

        if (dto.getMomentIds() != null) {
            for (Long momentId : dto.getMomentIds()) {
                if (!isNullOrNonPositive(momentId)) {
                    momentIds.add(momentId);
                }
            }
        }

        if (StringUtils.isNotBlank(dto.getMomentIdsText())) {
            String[] values = StringUtils.split(dto.getMomentIdsText(), ",");
            if (values != null) {
                for (String value : values) {
                    long momentId = NumberUtils.toLong(StringUtils.trim(value), 0L);
                    if (momentId > 0) {
                        momentIds.add(momentId);
                    }
                }
            }
        }

        return new ArrayList<>(momentIds);
    }

    public static long normalizeCursorMomentId(Long momentId) {
        if (isNullOrNonPositive(momentId)) {
            return Long.MAX_VALUE;
        }
        return momentId;
    }

    public static int normalizeMomentQueryLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_AUTHOR_MOMENT_LIMIT;
        }
        return Math.min(limit, MAX_AUTHOR_MOMENT_LIMIT);
    }

    /**
     * 存在当前类型的定义
     */
    public static boolean existType(String momentType) {
        return MOMENT_TYPE_LIST.contains(momentType);
    }
    /** null ， 小于等于0 是返回 true*/
    public static boolean isNullOrNonPositive(Number num) {
        return num == null ||num.doubleValue() <= 0;
    }

    public  static boolean isAllNull(Object... objects) {
        if (objects == null) {
            return true;
        }

        for (Object o : objects) {
            if (o != null) {
                return false;
            }
        }
        return true;
    }
}
