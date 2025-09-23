package com.escape.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.escape.backend.mapper")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

        System.out.println("欢迎来到Escape 的RolePlayingChat");
        System.out.println("测试连接是:http://127.0.0.1:8080/api/test");
        System.out.println("可以访问源码地址:https://github.com/ZeroTB01/Role-Playing-Chat.git");
    }

}
