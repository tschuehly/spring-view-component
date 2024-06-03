package de.tschuehly.example.jte;

import de.tschuehly.spring.viewcomponent.core.IntegrationTestBase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.view-component.local-development=true"
    }
)
// TODO: how to test local dev. org.springframework.boot.devtools.restart.OnInitializedRestarterCondition is false
public class JteIntegrationLocalDevTest extends IntegrationTestBase {

}
