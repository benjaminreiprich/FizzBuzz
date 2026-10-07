package io.github.benjaminreiprich.fizzbuzz.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

// Served at /v3/api-docs (OpenAPI JSON) and /swagger-ui.html (interactive documentation).
@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info =
                @Info(
                        title = "FizzBuzz REST API",
                        version = "v1",
                        description = "Configurable FizzBuzz: numbers from 1 to limit, multiples of int1 replaced by"
                                + " str1, multiples of int2 by str2, multiples of both by str1str2."))
public class OpenApiConfiguration {}
