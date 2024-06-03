package de.tschuehly.spring.viewcomponent.kte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ViewComponentAutoConfiguration::class, KteConfiguration::class)
class KteViewComponentAutoConfiguration {
}