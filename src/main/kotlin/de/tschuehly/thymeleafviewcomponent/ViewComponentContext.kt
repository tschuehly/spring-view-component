package de.tschuehly.thymeleafviewcomponent

data class ViewComponentContext(
    val context: Map<String,Any>,
    val componentClass: Class<*>? = null
) {
}