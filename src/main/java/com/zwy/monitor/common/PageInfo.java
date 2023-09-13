package com.zwy.monitor.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zwy
 * @date 2023/9/13 10:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo {
    private long total;

    private List<?> list;
}
