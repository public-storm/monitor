package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年06月27日 14:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DeleteDirectoryRequest {
    @NotBlank
    private String id;
    private String userId;
}
