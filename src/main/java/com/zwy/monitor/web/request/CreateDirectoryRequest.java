package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年05月17日 11:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CreateDirectoryRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String superId;

    private String userId;
}
