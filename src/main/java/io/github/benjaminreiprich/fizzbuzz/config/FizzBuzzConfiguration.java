package io.github.benjaminreiprich.fizzbuzz.config;

import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FizzBuzzProperties.class)
public class FizzBuzzConfiguration {

    // The domain has no Spring dependency, so its classes are declared as beans here rather than annotated.
    @Bean
    FizzBuzzGenerator fizzBuzzGenerator() {
        return new FizzBuzzGenerator();
    }
}
