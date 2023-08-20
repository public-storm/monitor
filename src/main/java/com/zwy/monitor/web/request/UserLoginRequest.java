package com.zwy.monitor.web.request;

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
    @NotBlank(message = "用户名不为空")
    private String userName;
    @NotBlank(message = "密码不为空")
    private String password;
}
