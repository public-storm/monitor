package com.zwy.monitor.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author zwy
 * @date 2022年04月14日 17:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserModel implements Serializable {
    private static final long serialVersionUID = 942780771196404746L;
    @ApiModelProperty("用户id")
    private String id;
    @ApiModelProperty("用户名")
    private String name;
}
