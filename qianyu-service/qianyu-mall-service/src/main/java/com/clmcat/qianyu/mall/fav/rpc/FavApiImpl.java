package com.clmcat.qianyu.mall.fav.rpc;

import com.clmcat.qianyu.mall.api.fav.FavApi;
import com.clmcat.qianyu.mall.fav.mapper.FavFavoriteMapper;
import com.clmcat.qianyu.mall.fav.model.dto.FavTargetDTO;
import com.clmcat.qianyu.mall.fav.model.entity.FavFavorite;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class FavApiImpl implements FavApi {

    @Resource
    private FavFavoriteMapper favMapper;

    @Override
    public boolean isFavored(Long userId, Long targetId, Integer type) {
        return favMapper.selectByUserAndTarget(userId, targetId, type) != null;
    }

    // ==================== Internal methods for ViewBiz ====================

    public FavFavorite selectByUserAndTarget(long userId, Long targetId, Integer type) {
        return favMapper.selectByUserAndTarget(userId, targetId, type);
    }

    public void insertSelective(FavFavorite fav) {
        favMapper.insertSelective(fav);
    }

    public void deleteById(Long id) {
        favMapper.deleteById(id);
    }

    public Page<FavFavorite> paginate(Page<FavFavorite> page, QueryWrapper qw) {
        return favMapper.paginate(page, qw);
    }

    public FavFavorite selectOneById(Long id) {
        return favMapper.selectOneById(id);
    }

    public List<FavFavorite> selectBatchByTargets(long userId, List<FavTargetDTO> targets) {
        List<FavFavorite> result = favMapper.selectBatchByTargets(userId, targets);
        return result != null ? result : new ArrayList<>();
    }
}
