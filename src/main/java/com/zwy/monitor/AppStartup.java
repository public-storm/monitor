package com.zwy.monitor;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author zwy
 */
@Configuration
@Order(value = 1)
@Slf4j
public class AppStartup implements ApplicationRunner {


    @Value("${file.path}")
    private String filePath;

    @Value("${video.path}")
    private String videoPath;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        //文件存储目录创建
        createLocalPath(filePath);
        //视频存储目录创建
        createLocalPath(videoPath);
        //初始化表
        createTable();
    }

    /**
     * 存储目录创建
     *
     * @param path 存储目录
     */
    private void createLocalPath(String path) {
        File file = new File(path);
        if (!file.exists() && !file.isDirectory()) {
            boolean create = file.mkdirs();
            log.info("file path create {}", create);
        }
    }

    private void createTable() {
        FileReader fileReader = new FileReader("classpath:sql/init.sql");
        String read = fileReader.readString();
        String[] sqlArr = read.split(";");
        for (String sql : sqlArr) {
            if (CharSequenceUtil.isNotBlank(sql)) {
                jdbcTemplate.execute(sql);
            }
        }
    }
}
