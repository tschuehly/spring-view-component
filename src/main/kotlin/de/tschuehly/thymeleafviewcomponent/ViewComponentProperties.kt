package de.tschuehly.thymeleafviewcomponent

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("viewcomponent")
class ViewComponentProperties(
    val localDevelopment: Boolean = false
)