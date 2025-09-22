package com.escape.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

        System.out.println("欢迎来到Escape 的RolePlayingChat");
        System.out.println("可以访问源码地址:https://github.com/ZeroTB01/Role-Playing-Chat.git");
    }

}
