package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.ViewProperty

class ViewContext(override val contextAttributes: Array<out ViewProperty>) :
    IViewContext {
    override var componentTemplate: String? = null
    override var componentBean: Any? = null


    constructor(
        vararg contextAttributes: ViewProperty,
        componentTemplate: String? = null
    ) : this(contextAttributes) {
        this.componentTemplate = componentTemplate

    }

}