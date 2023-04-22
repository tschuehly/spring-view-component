package de.tschuehly.spring.viewcomponent.core

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.viewcomponent")
class ViewComponentProperties(
    val localDevelopment: Boolean = false
)