package com.zwy.monitor.beanManager;

import com.zwy.monitor.bean.RedisFileBean;
import com.zwy.monitor.web.response.file.CheckExistsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zwy
 * @date 2022年06月13日 15:02
 */
@Mapper
public interface CheckExistsResponseManager {
    CheckExistsResponseManager INSTANCE = Mappers.getMapper(CheckExistsResponseManager.class);

    /**
     * 内部bean转换
     *
     * @param redisFile redisFile
     * @return com.example.myfile.web.response.SelectDirectoryResponse
     * @author zwy
     * @date 2022/5/18 0018 15:29
     */
    CheckExistsResponse toCheckExistsResponse(RedisFileBean redisFile);
}
