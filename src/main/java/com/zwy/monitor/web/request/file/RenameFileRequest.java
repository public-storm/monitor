package com.zwy.monitor.web.request.file;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年11月15日 13:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RenameFileRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String id;
    private String userId;
}
