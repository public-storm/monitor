package com.zwy.monitor.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author zwy
 * @date 2022年04月13日 16:53
 */
//@Component
public class MyWebMvcConfigurer implements WebMvcConfigurer {
    @Resource
    private MyInterceptor myInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(myInterceptor).addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/video1",
                        "/video2",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "**/swagger-ui.html",
                        "/swagger-ui.html/**"
                );
    }
}
