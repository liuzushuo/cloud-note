package com.cloudnote.record;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.cloudnote")
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class RecordApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecordApplication.class, args);
    }
}
