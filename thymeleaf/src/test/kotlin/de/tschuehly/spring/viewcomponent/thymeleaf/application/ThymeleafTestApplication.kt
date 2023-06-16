package de.tschuehly.spring.viewcomponent.thymeleaf.application

import de.tschuehly.spring.viewcomponent.thymeleaf.ThymeleafViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.thymeleaf.application.web.header.HeaderViewComponent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(ThymeleafViewComponentAutoConfiguration::class, ThymeleafAutoConfiguration::class)
class ThymeleafTestApplication


fun main(args: Array<String>) {
    runApplication<ThymeleafTestApplication>(*args)
}

