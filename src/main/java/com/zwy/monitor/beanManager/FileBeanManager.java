package com.zwy.monitor.beanManager;

import com.zwy.monitor.bean.dbBean.UserFile;
import com.zwy.monitor.web.response.SelectFileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author zwy
 * @date 2022年05月18日 11:27
 */
@Mapper
public interface FileBeanManager {
    FileBeanManager INSTANCE = Mappers.getMapper(FileBeanManager.class);

    /**
     * 内部bean转换
     *
     * @param directory directory
     * @return com.example.myfile.web.response.SelectDirectoryResponse
     * @author zwy
     * @date 2022/5/18 0018 15:29
     */
    @Mapping(target = "name", expression = "java(com.zwy.monitor.util.DesensitizedUtil.decrypt(directory.getName()) + directory.getSuffix())")
    SelectFileResponse toFileResponse(UserFile directory);
}
