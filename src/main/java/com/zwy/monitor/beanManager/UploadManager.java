package com.zwy.monitor.beanManager;

import com.zwy.monitor.bean.SaveBean;
import com.zwy.monitor.web.request.file.UploadSplitRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author zwy
 * @date 2022年06月14日 16:02
 */
@Mapper
public interface UploadManager {
    UploadManager INSTANCE = Mappers.getMapper(UploadManager.class);

    /**
     * 内部bean转换
     *
     * @param req UploadRequest
     * @return com.example.myfile.bean.SaveBean
     * @author zwy
     * @date 2022/6/14 0014 16:03
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    SaveBean toSaveBean(UploadSplitRequest req);
}
