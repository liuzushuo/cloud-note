package com.cloudnote.collect;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.cloudnote")
@MapperScan("com.cloudnote.collect.mapper")
@SpringBootApplication
public class CollectApplication {
    public static void main(String[] args) {
        SpringApplication.run(CollectApplication.class, args);
    }
}
