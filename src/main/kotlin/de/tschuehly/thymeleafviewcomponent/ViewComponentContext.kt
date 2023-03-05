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

    constructor(vararg contextAttributes: Pair<String, Any>) :
            this(*(contextAttributes.map { ModelPair(it.first, it.second) }.toTypedArray())) {
        this.componentTemplate = componentTemplate
    }

    constructor() : this(null)
}