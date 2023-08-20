package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zwy
 * @date 2022年07月08日 11:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FavoriteDirectoryRequest {
    @NotBlank
    private String id;

    @NotNull
    private Integer favorite;
}
