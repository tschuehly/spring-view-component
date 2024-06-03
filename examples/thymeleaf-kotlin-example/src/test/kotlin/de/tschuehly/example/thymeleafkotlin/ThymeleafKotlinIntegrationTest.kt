package de.tschuehly.example.thymeleafkotlin

import de.tschuehly.spring.viewcomponent.core.IntegrationTestBase
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.view-component.local-development=false"]
)
class ThymeleafKotlinIntegrationTest : IntegrationTestBase()