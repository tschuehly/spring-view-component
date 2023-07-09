package de.tschuehly.spring.viewcomponent.core.component

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.view-component")
class ViewComponentProperties(
    val localDevelopment: Boolean = false
)