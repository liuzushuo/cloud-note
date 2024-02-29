package com.cloudnote.thing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.cloudnote")
@MapperScan("com.cloudnote.thing.mapper")
@SpringBootApplication
public class ThingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThingApplication.class, args);
    }
}
