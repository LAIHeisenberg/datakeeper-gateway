package com.longmai.datakeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

@SpringCloudApplication
public class DatakeeperGatewayApplication {

    public static void main(String[] args){
        SpringApplication.run(DatakeeperGatewayApplication.class, args);
    }

}
