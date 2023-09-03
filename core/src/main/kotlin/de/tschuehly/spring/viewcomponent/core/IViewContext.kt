package de.tschuehly.spring.viewcomponent.core

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
        fun getComponentName(context: IViewContext): String {
            return context.javaClass.enclosingClass.simpleName.substringBefore("$$").lowercase()
        }

        fun getTemplate(context: IViewContext): String {
            val componentName = context.javaClass.enclosingClass.simpleName.substringBefore("$$")
            val componentPackage = context.javaClass.enclosingClass.`package`.name.replace(".", "/") + "/"
            return "$componentPackage$componentName$templateSuffx"
        }
    }
}