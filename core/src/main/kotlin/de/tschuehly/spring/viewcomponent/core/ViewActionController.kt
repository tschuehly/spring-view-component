package de.tschuehly.spring.viewcomponent.core

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

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
        //TODO: Endpoints need to be mapped to the endpoint below
    }


    @PostMapping("/{viewComponentName}/{viewActionMethodName}")
    fun serveViewActionEndpoint(
        @PathVariable viewComponentName: String,
        @PathVariable viewActionMethodName: String,
        @RequestParam formData: Map<String,String>,
        request: HttpServletRequest
    ): IViewContext {
        logger.info("ViewComponent: $viewComponentName, viewActionMethod: $viewActionMethodName")
        val viewActionData = viewActionMap["${viewComponentName.lowercase()}/${viewActionMethodName.lowercase()}"]
            ?: throw ViewActionNotFoundException()
        val params = viewActionData.viewActionMethod.parameterTypes
        if(params.size > 1) throw Error("ViewAction only supports 1 or 0 parameters")

        val returnValue = if(params.size == 1){
            val viewComponentFormDTO = viewActionData.viewActionMethod.parameterTypes.first()
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            val formDTO = mapper.convertValue(formData,viewComponentFormDTO)
            viewActionData.viewActionMethod.invoke(
                viewActionData.viewComponentObject,
                formDTO
            )
        } else {
            viewActionData.viewActionMethod.invoke(
                viewActionData.viewComponentObject
            )
        }
        if(returnValue::class.isSubclassOf(IViewContext::class)){
            return returnValue as IViewContext
        }
        throw Error("Not working")
    }

    fun String.lowercase(): String {
        return this.lowercase(Locale.getDefault())
    }
}