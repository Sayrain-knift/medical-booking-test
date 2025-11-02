package com.sayrain.medicalbooking.config;

import io.swagger.v3.oas.models.OpenAPI; // 来自正确的包
import io.swagger.v3.oas.models.info.Info;  // 来自正确的包
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Medical Booking API")
                        .version("1.0")
                        .description("API documentation for the Medical Booking system"));
    }
}
