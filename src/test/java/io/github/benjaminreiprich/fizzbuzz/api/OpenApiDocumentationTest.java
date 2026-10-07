package io.github.benjaminreiprich.fizzbuzz.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    private static final String FIZZBUZZ_GET = "$.paths['/api/v1/fizzbuzz'].get";

    private final MockMvc mockMvc;

    OpenApiDocumentationTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_document_the_fizzbuzz_endpoint_with_its_five_parameters() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(FIZZBUZZ_GET + ".parameters[*].name")
                        .value(containsInAnyOrder("int1", "int2", "limit", "str1", "str2")))
                .andExpect(jsonPath(FIZZBUZZ_GET + ".parameters[*].required").value(everyItem(is(true))))
                .andExpect(jsonPath(FIZZBUZZ_GET + ".parameters[*].description").value(everyItem(notNullValue())))
                .andExpect(jsonPath(FIZZBUZZ_GET + ".responses['200']").exists())
                .andExpect(jsonPath(FIZZBUZZ_GET + ".responses['400']").exists());
    }

    @Test
    void should_document_the_statistics_endpoint_without_parameters() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.paths['/api/v1/statistics'].get.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/statistics'].get.responses['200']")
                        .exists());
    }

    @Test
    void should_serve_swagger_ui() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
