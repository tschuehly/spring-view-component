package de.tschuehly.spring.viewcomponent.core

import org.springframework.context.ApplicationContext

interface IViewContext {
    companion object {
        var applicationContext: ApplicationContext? = null

        fun <T> server(clazz: Class<T>): T{
            return applicationContext?.getBean(clazz) ?: throw RuntimeException(clazz.simpleName)
        }


        fun getViewComponentTemplateWithoutSuffix(context: IViewContext): String {
            val componentName = getViewComponentName(context.javaClass)
            val componentPackage = context.javaClass.enclosingClass.`package`.name.replace(".", "/") + "/"
            return "$componentPackage$componentName"
        }
        fun getViewComponentName(viewContext: Class<out IViewContext>): String {
            val enclosingClass = viewContext.getEnclosingClass()
                ?: throw ViewContextException("Your ViewContext record/class needs to be defined in the ViewComponent")
            return enclosingClass.getSimpleName()
        }
    }

}