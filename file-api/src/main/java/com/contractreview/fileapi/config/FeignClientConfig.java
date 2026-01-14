package com.contractreview.fileapi.config;

import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Feign客户端配置类
 *
 * @author ContractReview Team
 * @version 1.0.0
 */
@Configuration
public class FeignClientConfig {

    /**
     * 配置文件上传编码器
     * 支持multipart/form-data格式的文件上传
     */
    @Bean
    @Primary
    public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    /**
     * 配置二进制数据解码器
     * 支持文件下载的byte[]返回类型
     */
    @Bean
    @Primary
    public Decoder feignDecoder() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // 添加字节数组转换器，支持二进制文件下载
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        converters.add(byteArrayConverter);

        // 添加字符串转换器
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        converters.add(stringConverter);

        // 添加JSON转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        converters.add(jsonConverter);

        ObjectFactory<HttpMessageConverters> messageConvertersObjectFactory = () -> new HttpMessageConverters(converters);

        return new ResponseEntityDecoder(new SpringDecoder(messageConvertersObjectFactory));
    }

    /**
     * 配置重试策略
     * 禁用重试，避免重复请求导致的问题
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer defaultRetryer() {
        return new Retryer.Default(100, 1000, 0);
    }
}