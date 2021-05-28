package com.onenet.studio.sdk.sample.config;

import com.onenet.studio.acc.sdk.OpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: fanhaiqiu
 * @date: 2020/12/24
 */
@Configuration
public class BeanConfiguration {

    @Value("${sdk.api.url}")
    private String url;

    @Value("${sdk.api.product-id}")
    private String productId;

    @Value("${sdk.api.dev-key}")
    private String devKey;

    @Value("${sdk.api.access-key}")
    private String accessKey;

    @Bean(destroyMethod = "disconnect")
    public OpenApi openApi() throws Exception {
//        ClassPathResource classPathResource = new ClassPathResource("certificate.pem");
//        InputStream inputStream = classPathResource.getInputStream();
//        byte[] caCrtFile = IOUtils.toByteArray(inputStream);
        return OpenApi.Builder.newBuilder()
                .url(url)
                .productId(productId)
                .devKey(devKey)
                .accessKey(accessKey)
//                .caCrtFile(caCrtFile)
                .build();
    }
}
