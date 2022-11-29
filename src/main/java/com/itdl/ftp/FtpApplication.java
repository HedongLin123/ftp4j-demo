package com.itdl.ftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Description
 * @Author itdl
 * @Date 2022/11/25 10:44
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FtpApplication {
    public static void main(String[] args) {
        SpringApplication.run(FtpApplication.class, args);
    }
}
