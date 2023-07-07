package de.tschuehly.spring.viewcomponent.core

import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.lang.reflect.Method

@Configuration
class ViewActionConfiguration(val context: ApplicationContext,
                              val requestMappingHandlerMapping: RequestMappingHandlerMapping){

    @PostConstruct
    fun registerViewActionEndpoints() {
        val viewComponentBeans = context.getBeansWithAnnotation(ViewComponent::class.java)

        viewComponentBeans.forEach { (viewComponentName, viewComponentBean) ->
            val beanType = ClassUtils.getUserClass(viewComponentBean.javaClass)
            val viewComponentMethods = beanType.methods
            processViewComponentBean(viewComponentMethods, viewComponentName, viewComponentBean)
        }
    }

    private fun processViewComponentBean(
        viewComponentMethods: Array<out Method>,
        viewComponentName: String,
        viewComponentBean: Any
    ) {
        viewComponentMethods.forEach { method ->
            processViewComponentMethods(method,viewComponentName,viewComponentBean)
        }
    }

    private fun processViewComponentMethods(method: Method, viewComponentName: String, viewComponentBean: Any) {
        method.declaredAnnotations.forEach { declaredAnnotation ->
            val pathMapping = when (declaredAnnotation.annotationClass) {
                PostViewAction::class -> RequestMethod.POST toPath (declaredAnnotation as PostViewAction).path
                GetViewAction::class -> RequestMethod.GET toPath(declaredAnnotation as GetViewAction).path
                PutViewAction::class -> RequestMethod.PUT toPath (declaredAnnotation as PutViewAction).path
                PatchViewAction::class -> RequestMethod.PATCH toPath(declaredAnnotation as PatchViewAction).path
                DeleteViewAction::class -> RequestMethod.DELETE toPath(declaredAnnotation as DeleteViewAction).path
                else -> return@forEach
            }
            createRequestMappingForAnnotation(
                viewComponentName = viewComponentName,
                viewComponentBean = viewComponentBean,
                mapping = pathMapping,
                method = method
            )
        }
    }

    class PathMapping(
        val requestMethod: RequestMethod,
        val path: String
    )

    infix fun RequestMethod.toPath(that: String) = PathMapping(this, that)

    private fun createRequestMappingForAnnotation(
        viewComponentName: String,
        viewComponentBean: Any,
        mapping: PathMapping,
        method: Method,
    ) {

        val path = if (mapping.path == "") {
            "/${viewComponentName.lowercase()}/${method.name}".lowercase()
        } else mapping.path.lowercase()
        val options = RequestMappingInfo.BuilderConfiguration();
        val parser = PathPatternParser();
        parser.isCaseSensitive = false
        options.patternParser = parser
        requestMappingHandlerMapping.registerMapping(
            RequestMappingInfo.paths(path)
                .methods(mapping.requestMethod).options(options).build(),
            viewComponentBean,
            method
        )
    }

}
