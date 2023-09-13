package com.zwy.monitor.beanManager;

import com.zwy.monitor.bean.dbBean.UserMovie;
import com.zwy.monitor.web.response.movie.FindUserMovieResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author zwy
 * @date 2023/9/13 10:35
 */
@Mapper
public interface UserMovieBeanManager {
    UserMovieBeanManager INSTANCE = Mappers.getMapper(UserMovieBeanManager.class);

    @Mapping(target = "name", expression = "java(com.zwy.monitor.util.DesensitizedUtil.decrypt(bean.getName()) + bean.getSuffix())")
    FindUserMovieResponse toFindUserMovieResponse(UserMovie bean);
}
