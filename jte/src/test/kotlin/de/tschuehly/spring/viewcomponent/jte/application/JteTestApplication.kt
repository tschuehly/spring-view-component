package de.tschuehly.spring.viewcomponent.jte.application

import de.tschuehly.spring.viewcomponent.jte.JteViewComponentAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(JteViewComponentAutoConfiguration::class)
class JteTestApplication


fun main(args: Array<String>) {
    runApplication<JteTestApplication>(*args)
}
