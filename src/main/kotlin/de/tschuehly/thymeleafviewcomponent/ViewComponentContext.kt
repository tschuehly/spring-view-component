package de.tschuehly.thymeleafviewcomponent


class ViewComponentContext(
    vararg val contextAttributes: ModelPair,
) {
    var componentTemplate: String? = null

    constructor(
        componentTemplate: String? = null, vararg contextAttributes: ModelPair
    ) : this(*contextAttributes) {
        this.componentTemplate = componentTemplate
    }

}