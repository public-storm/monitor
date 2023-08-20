package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年05月17日 14:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SelectFileRequest {
    @NotBlank
    private String superId;
    private String userId;
}
