package com.zwy.monitor.web.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2023/5/27 19:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequest {
    @NotBlank(message = "用户名不为空")
    private String userName;
    @NotBlank(message = "密码不为空")
    private String password;
}
