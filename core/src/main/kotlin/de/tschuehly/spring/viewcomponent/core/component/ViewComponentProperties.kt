package de.tschuehly.spring.viewcomponent.core.component

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.view-component")
data class ViewComponentProperties(
    val localDevelopment: Boolean = false,
    val viewComponentRoot: String = "src/main/java",
    val standaloneTemplateRoot: String = "src/main/resources",
    val templateSuffix: String
)