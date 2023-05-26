package de.tschuehly.spring.viewcomponent.core

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.util.ClassUtils
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.lang.reflect.Method

@Configuration
class ViewActionConfiguration(val context: ApplicationContext,
                              val requestMappingHandlerMapping: RequestMappingHandlerMapping){
    val viewActionMap = mutableMapOf<String, ViewActionData>()

    class ViewActionData(val httpMethod: HttpMethod, val viewComponentObject: Any, val viewActionMethod: Method)

    @PostConstruct
    fun registerViewActionEndpoints() {
        val viewComponentBeans = context.getBeansWithAnnotation(ViewComponent::class.java)
        viewComponentBeans.forEach { (viewComponentName, viewComponentBean) ->
            val beanType = ClassUtils.getUserClass(viewComponentBean.javaClass)
            val viewComponentMethods = beanType.methods
            viewComponentMethods.forEach { method ->
                method.declaredAnnotations.find {
                    it.annotationClass == ViewAction::class
                }?.let { declaredAnnotation  ->
                    val viewAction = (declaredAnnotation as ViewAction)
                    val path = if(viewAction.path == ""){
                        "/${viewComponentName}/${method.name}".lowercase()
                    } else viewAction.path.lowercase()

                    viewActionMap[path] =
                        ViewActionData(viewAction.method.asHttpMethod(), viewComponentBean, method)
                    val options = RequestMappingInfo.BuilderConfiguration();
                    val parser = PathPatternParser();
                    parser.isCaseSensitive = false
                    options.patternParser = parser
                    requestMappingHandlerMapping.registerMapping(
                        RequestMappingInfo.paths("/${viewComponentName.lowercase()}/${method.name.lowercase()}").methods(viewAction.method).options(options).build(),
                        viewComponentBean,
                        method
                    )
                }
            }
        }
    }
}
