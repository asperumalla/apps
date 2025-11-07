package com.example.paymentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add logging interceptor
        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            if (body != null && body.length > 0) {
                try {
                    String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(objectMapper.readValue(body, Object.class));
                    log.debug("Request Body:\n{}", prettyJson);
                } catch (Exception e) {
                    log.debug("Request Body: {}", new String(body));
                }
            }
            
            var response = execution.execute(request, body);
            
            return response;
        };
        
        restTemplate.setInterceptors(List.of(loggingInterceptor));
        return restTemplate;
    }
}

