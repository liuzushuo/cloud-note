package com.cloudnote.note;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.cloudnote")
@MapperScan("com.cloudnote.note.mapper")
@SpringBootApplication
public class NoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(NoteApplication.class, args);
    }
}
