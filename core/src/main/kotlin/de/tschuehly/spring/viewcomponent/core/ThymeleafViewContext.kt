package de.tschuehly.spring.viewcomponent.core

open class ThymeleafViewContext(
    open vararg val contextAttributes: ViewProperty,
) {
    var componentTemplate: String? = null


    constructor(
        componentTemplate: String? = null,
        vararg contextAttributes: ViewProperty
    ) : this(*contextAttributes) {
        this.componentTemplate = componentTemplate

    }

}