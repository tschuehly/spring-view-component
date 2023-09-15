package de.tschuehly.spring.viewcomponent.core.component

import de.tschuehly.spring.viewcomponent.core.IViewContext
import java.util.*

object ViewComponentUtils {
    @JvmStatic
    fun getId(viewContext: Class<out IViewContext>): String {
        return viewContext.getEnclosingClass().getSimpleName().lowercase(Locale.getDefault())
    }

    @JvmStatic
    fun getTarget(viewContext: Class<out IViewContext?>): String {
        return "#${viewContext.getEnclosingClass().getSimpleName().lowercase()}"
    }
}
