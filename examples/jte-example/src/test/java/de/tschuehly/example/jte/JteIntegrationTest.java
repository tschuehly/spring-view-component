package de.tschuehly.example.jte;

import de.tschuehly.spring.viewcomponent.core.IntegrationTestBase;
import de.tschuehly.spring.viewcomponent.core.JteIntegrationTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class JteIntegrationTest extends JteIntegrationTestBase {
    @Autowired
    TestRestTemplate testRestTemplate;
}
