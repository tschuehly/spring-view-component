package de.tschuehly.example.thymeleafjava;

import de.tschuehly.spring.viewcomponent.core.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.view-component.local-development=true"}
)
class ThymeleafJavaIntegrationLocalDevTest extends IntegrationTestBase {
}