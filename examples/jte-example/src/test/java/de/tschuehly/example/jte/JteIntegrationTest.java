package de.tschuehly.example.jte;

import de.tschuehly.spring.viewcomponent.core.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class JteIntegrationTest extends IntegrationTestBase {

  @Autowired
  TestRestTemplate testRestTemplate;
}
