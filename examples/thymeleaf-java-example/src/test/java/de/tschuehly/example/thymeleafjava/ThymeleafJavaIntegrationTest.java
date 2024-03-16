package de.tschuehly.example.thymeleafjava;

import de.tschuehly.spring.viewcomponent.core.ThymeleafIntegrationTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ThymeleafJavaIntegrationTest extends ThymeleafIntegrationTestBase {
    @Autowired
    TestRestTemplate testRestTemplate;
}