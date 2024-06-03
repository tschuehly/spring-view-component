package de.tschuehly.kteviewcomponentexample

import de.tschuehly.spring.viewcomponent.core.IntegrationTestBase
import org.junit.jupiter.api.Disabled
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.view-component.local-development=true"]
)
class KteIntegrationLocalDevTest : IntegrationTestBase()

