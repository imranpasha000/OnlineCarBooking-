package com.boxcars.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient userServiceClient(
            RestClient.Builder builder,
            @Value("${app.user-service.url:http://localhost:8082}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
