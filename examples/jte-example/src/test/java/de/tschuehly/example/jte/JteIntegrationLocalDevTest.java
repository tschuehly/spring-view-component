package de.tschuehly.example.jte;

import de.tschuehly.spring.viewcomponent.core.JteIntegrationTestBase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.view-component.local-development=true"}
)
public class JteIntegrationLocalDevTest extends JteIntegrationTestBase {

}
