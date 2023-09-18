package de.tschuehly.spring.viewcomponent.core

import de.tschuehly.spring.viewcomponent.core.component.ViewComponentUtils

interface IViewContext {
    companion object {
        var componentBean: Any? = null
        var componentTemplate: String? = null
        var jteTemplateEngine: Any? = null
        var templateSuffx: String = ""
        fun getAttributes(context: IViewContext): Map<String, Any> {
            return context::class.java.declaredFields.map { field ->
                context::class.java.getDeclaredField(field.name).let {
                    it.isAccessible = true
                    field.name to it[context]
                }
            }.toMap()
        }

        fun getViewComponentName(context: IViewContext): String {
            return ViewComponentUtils.getName(context.javaClass)
        }

        fun getViewComponentTemplate(context: IViewContext): String {
            return getViewComponentTemplateWithoutSuffix(context) + templateSuffx
        }

        fun getViewComponentTemplateWithoutSuffix(context: IViewContext): String {
            val componentName = ViewComponentUtils.getName(context.javaClass)
            val componentPackage = context.javaClass.enclosingClass.`package`.name.replace(".", "/") + "/"
            return "$componentPackage$componentName"
        }
    }
}