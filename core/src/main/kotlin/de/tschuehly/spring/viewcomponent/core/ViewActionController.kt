package de.tschuehly.spring.viewcomponent.core

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.full.isSubclassOf

@Controller
class ViewActionController(val context: ApplicationContext) {
    private val logger = LoggerFactory.getLogger(ViewActionController::class.java)

    val viewActionMap = mutableMapOf<String, ViewActionData>()

    class ViewActionData(val viewComponentObject: Any, val viewActionMethod: Method)

    @PostConstruct
    fun registerViewActionEndpoints() {
        val viewComponentBeans = context.getBeansWithAnnotation(ViewComponent::class.java)
        viewComponentBeans.forEach { (viewComponentName, viewComponentBean) ->
            val beanType = ClassUtils.getUserClass(viewComponentBean.javaClass)
            val viewComponentMethods = beanType.methods
            viewComponentMethods.forEach { method ->
                method.declaredAnnotations.any {
                    it.annotationClass == ViewAction::class
                }.let { methodIsViewAction ->
                    if(methodIsViewAction){
                        viewActionMap["${viewComponentName.lowercase()}/${method.name.lowercase()}"] =
                            ViewActionData(viewComponentBean,method)
                    }
                }
            }
        }
        println(viewActionMap)

    }

    @PostMapping("/{viewComponentName}/{viewActionMethodName}")
    fun serveViewActionEndpoint(
        @PathVariable viewComponentName: String,
        @PathVariable viewActionMethodName: String
    ): IViewContext {
        logger.info("ViewComponent: $viewComponentName, viewActionMethod: $viewActionMethodName")
        val viewActionData = viewActionMap["${viewComponentName.lowercase()}/${viewActionMethodName.lowercase()}"]
            ?: throw ViewActionNotFoundException()
        val returnValue = viewActionData.viewActionMethod.invoke(
            viewActionData.viewComponentObject
        )
        logger.error(returnValue.toString())
        if(returnValue::class.isSubclassOf(IViewContext::class)){
            return returnValue as IViewContext
        }
        throw Error("Not working")
    }

    fun String.lowercase(): String {
        return this.lowercase(Locale.getDefault())
    }
}