package com.longmai.datakeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableFeignClients(basePackages={"com.longmai.datakeeper.rest"})
public class DatakeeperGatewayApplication {

    public static void main(String[] args){
        SpringApplication.run(DatakeeperGatewayApplication.class, args);
    }

}
