package com.example.remotecamera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemoteCameraApplication {
    public static void main(String[] args) { SpringApplication.run(RemoteCameraApplication.class, args); }
}
