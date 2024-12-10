package dev.cxl.Storage.Service.configuration;



import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule localDateTimeModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new CustomLocalDateDeserializer());
        return simpleModule;
    }
}
