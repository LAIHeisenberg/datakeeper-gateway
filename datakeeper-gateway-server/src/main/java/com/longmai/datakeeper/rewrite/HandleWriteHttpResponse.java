package com.longmai.datakeeper.rewrite;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.longmai.datakeeper.rest.api.ApiMaskingHandler;
import com.longmai.datakeeper.rest.dto.ApiFieldMaskingDto;
import com.longmai.datakeeper.rest.dto.ApiMaskingDetailDto;
import io.netty.util.internal.StringUtil;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            List<ApiFieldMaskingDto> fields = apiMaskingDetail.getFields();
            if (!StringUtil.isNullOrEmpty(messageBody)) {
                JSONObject respJson = JSONObject.parseObject(messageBody);
                JSONArray content = respJson.getJSONArray("content");
                Iterator<Object> iterator = content.iterator();
                while (iterator.hasNext()){
                    JSONObject next = (JSONObject) iterator.next();
                    fields.stream().forEach(new Consumer<ApiFieldMaskingDto>() {
                        @Override
                        public void accept(ApiFieldMaskingDto apiFieldMaskingDto) {
                            String fieldName = apiFieldMaskingDto.getFieldName();
                            String val = next.getString(fieldName);
                            if (StringUtils.isEmpty(val)){
                                return;
                            }
                            String regex = apiFieldMaskingDto.getRegex();
                            Pattern compile = Pattern.compile(regex);
                            Matcher matcher = compile.matcher(val);
                            if (matcher.find()){
                                String group = matcher.group(1);
                                StringBuffer sbf = new StringBuffer();
                                for (int i=0; i<group.length(); i++){
                                    sbf.append(apiFieldMaskingDto.getMaskingSymbol());
                                }
                                next.put(fieldName, val.replace(group, sbf.toString()));
                            }
                        }
                    });

                }
                respJson.put("content", content);
                messageBody = respJson.toJSONString();
            }
        }
        return Mono.just(messageBody);
    }




}
