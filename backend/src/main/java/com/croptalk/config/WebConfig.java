package com.croptalk.config;

import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Node: app.use(cors()) - default options: any origin, GET/HEAD/PUT/PATCH/POST/DELETE, request headers reflected. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "PUT", "PATCH", "POST", "DELETE")
                .allowedHeaders("*");
    }

    /** Mongoose serialises ObjectId as its 24-char hex string in JSON responses. */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectIdSerializer() {
        return builder -> builder.serializerByType(ObjectId.class, new ToStringSerializer());
    }
}
