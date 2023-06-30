package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年04月14日 22:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserLoginRequest {
    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不为空")
    private String userName;
    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不为空")
    private String password;
}
