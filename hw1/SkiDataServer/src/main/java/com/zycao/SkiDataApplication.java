package com.zycao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class SkiDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkiDataApplication.class, args);
    }

}
