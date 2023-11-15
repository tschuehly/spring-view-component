package de.tschuehly.spring.viewcomponent.core.component

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.view-component")
data class ViewComponentProperties(
    val localDevelopment: Boolean = false,
    val jteTemplateDirectories: List<String> = listOf("src/main/java","src/main/kotlin"),
    val jteTemplateSuffix: String = ".jte"
)