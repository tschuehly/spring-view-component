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

        /**
         * Get the script path for a component's TypeScript/JavaScript file
         * Convention: /js/components/{package}/{ComponentName}.js
         */
        fun getComponentScriptPath(context: IViewContext): String {
            val componentPath = getViewComponentTemplateWithoutSuffix(context)
            return "/js/components/$componentPath.js"
        }

        /**
         * Get all script paths for a component
         * Can be extended to support multiple scripts per component
         */
        fun getComponentScripts(context: IViewContext): List<String> {
            return listOf(getComponentScriptPath(context))
        }
    }

    /**
     * Get the script path for this component
     * Returns the conventional path where the compiled TypeScript file is located
     */
    fun getScriptPath(): String {
        return getComponentScriptPath(this)
    }

    /**
     * Get all script paths for this component
     * Override this method to add additional scripts
     */
    fun getScripts(): List<String> {
        return getComponentScripts(this)
    }

}