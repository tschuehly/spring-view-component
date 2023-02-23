package de.tschuehly.thymeleafviewcomponent

class ViewComponentContext(
    val context: Map<String,Any>,
    val componentClass: Class<*>? = null
) {
}