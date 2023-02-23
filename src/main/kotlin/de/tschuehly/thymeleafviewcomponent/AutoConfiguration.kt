package de.tschuehly.thymeleafviewcomponent

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("de.tschuehly.thymeleafviewcomponent")
class AutoConfiguration(
    val applicationContext: ApplicationContext
) {
    @Bean
    fun createViewComponentConverter():
            ViewComponentConverter {
        return ViewComponentConverter(applicationContext)
    }
}