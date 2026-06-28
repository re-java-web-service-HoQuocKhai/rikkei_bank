package com.re.rikkei_bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rikkeiBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rikkei Bank API")
                        .description("REST API documentation for Rikkei Bank Application")
                        .version("v1.0.0"));
    }
}
