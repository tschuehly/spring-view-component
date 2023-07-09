package de.tschuehly.jteviewcomponentdemo.application

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import gg.jte.springframework.boot.autoconfigure.JteAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(ViewComponentAutoConfiguration::class, JteAutoConfiguration::class)
class JteTestApplication

fun main(args: Array<String>) {
    runApplication<JteTestApplication>(*args)
}

