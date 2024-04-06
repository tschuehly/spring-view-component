package de.tschuehly.example.thymeleafkotlin

import de.tschuehly.spring.viewcomponent.core.JteIntegrationTestBase
import de.tschuehly.spring.viewcomponent.core.ThymeleafIntegrationTestBase
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ThymeleafKotlinIntegrationTest : ThymeleafIntegrationTestBase()