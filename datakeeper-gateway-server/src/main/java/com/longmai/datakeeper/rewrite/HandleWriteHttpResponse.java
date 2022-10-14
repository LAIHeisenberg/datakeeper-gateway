package com.longmai.datakeeper.rewrite;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.longmai.datakeeper.rest.api.ApiMaskingHandler;
import com.longmai.datakeeper.rest.dto.ApiMaskingDetailDto;
import io.netty.util.internal.StringUtil;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

@Component(value = "HandleWriteHttpResponse")
public class HandleWriteHttpResponse implements RewriteFunction<String,String> {
    
    @Autowired
    private ApiMaskingHandler apiMaskingHandler;
    
    @Override
    public Publisher<String> apply(ServerWebExchange serverWebExchange, String messageBody) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        URI uri = request.getURI();
        String path = uri.getPath();

        System.out.println("path: " + path);
        HttpHeaders headers = serverWebExchange.getResponse().getHeaders();
        List<String> needMaskingHeader = headers.get("X-Need-Masking");
        if (!CollectionUtils.isEmpty(needMaskingHeader)){
            ApiMaskingDetailDto apiMaskingDetail = apiMaskingHandler.getApiMaskingDetail(path);
            if (!StringUtil.isNullOrEmpty(messageBody)) {
                JSONObject respJson = JSONObject.parseObject(messageBody);
                JSONArray content = respJson.getJSONArray("content");
                Iterator<Object> iterator = content.iterator();
                while (iterator.hasNext()){
                    JSONObject next = (JSONObject) iterator.next();
                    String phone = next.getString("phone");
                    if(!StringUtil.isNullOrEmpty(phone)){
                        String hideStr = phone.substring(3, 8);
                        phone = phone.replace(hideStr,"******");
                        next.put("phone", phone);
                    }
                    String email = next.getString("email");
                    String hideEmailStr = email.substring(1, email.indexOf("@"));
                    email = email.replace(hideEmailStr,"****");
                    next.put("email", email);
                }
                respJson.put("content", content);
                messageBody = respJson.toJSONString();
            }
        }
        return Mono.just(messageBody);
    }
}
