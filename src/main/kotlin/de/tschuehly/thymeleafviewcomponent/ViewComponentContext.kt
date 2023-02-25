package de.tschuehly.thymeleafviewcomponent

data class ViewComponentContext(
    val context: Map<String,Any>,
    val componentTemplate: String? = null
) {
}