package de.tschuehly.spring.viewcomponent.core.component

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.ViewContextException
import java.util.*

object ViewComponentUtils {
    @JvmStatic
    fun getName(viewContext: Class<out IViewContext>): String {
        val enclosingClass = viewContext.getEnclosingClass()
            ?: throw ViewContextException("Your ViewContext record/class needs to be defined in the ViewComponent")
        return enclosingClass.getSimpleName()
    }

    @JvmStatic
    fun getId(viewContext: Class<out IViewContext>): String {
        return getName(viewContext).lowercase()
    }

    @JvmStatic
    fun getTarget(viewContext: Class<out IViewContext>): String {
        return "#${getName(viewContext).lowercase()}"
    }

}
