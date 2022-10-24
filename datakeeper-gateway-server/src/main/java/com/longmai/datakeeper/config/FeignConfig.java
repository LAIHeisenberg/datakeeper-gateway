package com.longmai.datakeeper.config;

import com.longmai.datakeeper.rest.api.ApiMaskingHandler;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class FeignConfig {

    @Value("${feign.client.url}")
    private String client_url;

    @Bean
    public ApiMaskingHandler getApiMaskingHandler(){
        ApiMaskingHandler target = Feign.builder().encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(ApiMaskingHandler.class, client_url);
        return target;
    }

}

