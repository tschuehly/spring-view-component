package de.tschuehly.thymeleafviewcomponent


class ViewComponentContext(
    vararg val contextAttributes: Pair<String,Any>,
) {
    var componentTemplate: String? = null
    constructor(
        componentTemplate: String? = null, vararg contextAttributes: Pair<String, Any>) : this(*contextAttributes){
            this.componentTemplate = componentTemplate
        }

}