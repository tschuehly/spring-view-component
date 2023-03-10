package de.tschuehly.thymeleafviewcomponent

class ViewContext(
    vararg val contextAttributes: ViewProperty,
) {
    var componentTemplate: String? = null

    constructor(
        componentTemplate: String? = null,
        vararg contextAttributes: ViewProperty
    ) : this(*contextAttributes) {
        this.componentTemplate = componentTemplate

    }

}