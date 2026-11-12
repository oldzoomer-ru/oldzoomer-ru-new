package ru.oldzoomer.www.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.DeserializationFeature;

@Configuration
public class MappingConfig {
    @Bean JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
    }
}
