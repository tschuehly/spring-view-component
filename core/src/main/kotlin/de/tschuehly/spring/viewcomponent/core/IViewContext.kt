package de.tschuehly.spring.viewcomponent.core

interface IViewContext {
    companion object {
        var componentBean: Any? = null
        var componentTemplate: String? = null
        fun getAttributes(context: IViewContext): Map<String, Any> {
            return context::class.java.declaredFields.map { field ->
                context::class.java.getDeclaredField(field.name).let {
                    it.isAccessible = true
                    field.name to it[context]
                }
            }.toMap()
        }
    }
}

class EmptyViewContext() : IViewContext