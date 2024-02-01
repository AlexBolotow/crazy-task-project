package com.bolotov.crazy.task.tracker.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(
                    List.of(
                        new Server().url("http://localhost:8081")
                    )
                )
                .info(
                        new Info().title("Task-tracker API")
                );
    }

    //http://localhost:8081/swagger-ui/index.html#/
}
