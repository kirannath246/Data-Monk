package com.knowledge.datasource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan("com")
@EnableJpaRepositories("com")
@Slf4j
public class DatasourceSpringConfiguration {

    @Bean
    public ExternalDataSourceFactory externalDataSourceFactory() {
        log.info("Creating ExternalDataSourceFactory Bean" );
        return new ExternalDataSourceFactory();
    }
}
