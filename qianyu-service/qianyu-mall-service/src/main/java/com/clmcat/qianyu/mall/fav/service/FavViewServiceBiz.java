package com.clmcat.qianyu.mall.fav.service;

import com.clmcat.qianyu.mall.fav.model.dto.FavBatchCancelDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavBatchStatusDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavListQueryDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavTargetDTO;
import com.clmcat.qianyu.mall.fav.model.vo.*;
import com.mybatisflex.core.paginate.Page;
import java.util.*;

public interface FavViewServiceBiz {

    FavActionResultVO addFav(long userId, FavTargetDTO dto);

    FavActionResultVO cancelFav(long userId, FavTargetDTO dto);

    Page<FavItemVO> getFavList(long userId, FavListQueryDTO dto);

    FavStatusVO getFavStatus(long userId, FavTargetDTO dto);

    FavBatchStatusResultVO getBatchFavStatus(long userId, FavBatchStatusDTO dto);

    FavBatchCancelResultVO batchCancelFav(long userId, FavBatchCancelDTO dto);

}