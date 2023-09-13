package com.zwy.monitor.beanManager;

import com.zwy.monitor.bean.UserModel;
import com.zwy.monitor.bean.dbBean.User;
import com.zwy.monitor.web.request.UserLoginRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author zwy
 * @date 2022年04月18日 13:29
 */
@Mapper
public interface UserBeanManager {

    UserBeanManager INSTANCE = Mappers.getMapper( UserBeanManager.class );
    /**
     *  内部bean转换
     * @author zwy 
     * @date 2022/4/18 0018 13:31
     * @param req req
     * @return com.example.myfile.bean.User 
     */
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toUser(UserLoginRequest req);

}
