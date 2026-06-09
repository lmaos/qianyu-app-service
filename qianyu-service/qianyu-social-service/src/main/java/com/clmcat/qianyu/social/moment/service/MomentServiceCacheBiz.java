package com.clmcat.qianyu.social.moment.service;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Moment 查询缓存：
 * 1. 自己看自己的数据时绕过缓存；
 * 2. 查看别人的动态详情和作者列表时优先命中本地缓存。
 */
@Service
public class MomentServiceCacheBiz {
    private static final Duration DETAIL_CACHE_TTL = Duration.ofSeconds(30);
    private static final Duration AUTHOR_LIST_CACHE_TTL = Duration.ofSeconds(30);

    private final Cache<Long, MomentDto> momentDetailCache = Caffeine.newBuilder()
            .maximumSize(2_048)
            .expireAfterWrite(DETAIL_CACHE_TTL)
            .build();

    private final Cache<AuthorMomentListCacheKey, List<MomentDto>> authorMomentListCache = Caffeine.newBuilder()
            .maximumSize(1_024)
            .expireAfterWrite(AUTHOR_LIST_CACHE_TTL)
            .build();

    @Resource
    private MomentServiceBiz momentServiceBiz;

    /**
     * 查询动态详情缓存。
     *
     * @param viewerId 当前查看者ID；当查看者与作者相同时绕过缓存
     * @param momentId 动态ID
     * @return 动态 DTO，不存在时返回 null
     */
    public MomentDto getMoment(long viewerId, long momentId) {
        MomentDto cachedMoment = momentDetailCache.getIfPresent(momentId);
        if (cachedMoment != null && !Objects.equals(cachedMoment.getAuthorId(), viewerId)) {
            return cachedMoment;
        }

        MomentDto momentDto = momentServiceBiz.getMomentById(momentId);
        if (momentDto == null) {
            momentDetailCache.invalidate(momentId);
            return null;
        }

        if (Objects.equals(momentDto.getAuthorId(), viewerId)) {
            momentDetailCache.invalidate(momentId);
            return momentDto;
        }

        momentDetailCache.put(momentId, momentDto);
        return momentDto;
    }

    /**
     * 查询作者动态列表缓存。
     *
     * @param viewerId 当前查看者ID；本人查看自己的列表时绕过缓存
     * @param authorId 作者ID
     * @param nextMomentId 游标 dynamic ID，仅查询小于该值的数据
     * @param limit 查询数量
     * @return 动态 DTO 列表
     */
    public List<MomentDto> getMomentByAuthorId(long viewerId, long authorId, long nextMomentId, int limit) {
        if (Objects.equals(viewerId, authorId)) {
            return momentServiceBiz.getMomentByAuthorId(authorId, nextMomentId, limit).getMoments();
        }

        AuthorMomentListCacheKey cacheKey = new AuthorMomentListCacheKey(authorId, nextMomentId, limit);
        List<MomentDto> cachedMomentList = authorMomentListCache.getIfPresent(cacheKey);
        if (cachedMomentList != null) {
            return new ArrayList<>(cachedMomentList);
        }

        List<MomentDto> momentDtos = momentServiceBiz.getMomentByAuthorId(authorId, nextMomentId, limit).getMoments();
        List<MomentDto> cacheValue = new ArrayList<>(momentDtos);
        authorMomentListCache.put(cacheKey, cacheValue);

        for (MomentDto momentDto : momentDtos) {
            if (momentDto != null && !Objects.equals(momentDto.getAuthorId(), viewerId)) {
                momentDetailCache.put(momentDto.getMomentId(), momentDto);
            }
        }

        return momentDtos;
    }

    /**
     * 失效单条动态及其作者列表缓存。
     *
     * @param momentId 动态ID
     * @param authorId 动态作者ID
     */
    public void evictMoment(long momentId, long authorId) {
        momentDetailCache.invalidate(momentId);
        evictAuthorMomentList(authorId);
    }

    /**
     * 失效某个作者的所有动态列表缓存。
     *
     * @param authorId 作者ID
     */
    public void evictAuthorMomentList(long authorId) {
        authorMomentListCache.asMap().keySet().removeIf(cacheKey -> cacheKey.authorId() == authorId);
    }

    private record AuthorMomentListCacheKey(long authorId, long nextMomentId, int limit) {
    }
}
