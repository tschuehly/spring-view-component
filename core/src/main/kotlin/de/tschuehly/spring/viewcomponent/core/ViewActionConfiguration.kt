package de.tschuehly.spring.viewcomponent.core

import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.lang.reflect.Method

@Configuration
class ViewActionConfiguration(
    val context: ApplicationContext,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    val requestMappingHandlerMapping: RequestMappingHandlerMapping
) {

    val viewActionMapping = mutableMapOf<String, PathMapping>()
    //TODO: extract to seperate class/registry with unique constraints etc

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
            processViewComponentMethods(method, viewComponentName, viewComponentBean)
        }
    }

    private fun processViewComponentMethods(method: Method, viewComponentName: String, viewComponentBean: Any) {
        method.declaredAnnotations.forEach { declaredAnnotation ->
            val viewActionPair = when (declaredAnnotation.annotationClass) {
                GetViewAction::class -> RequestMethod.GET to (declaredAnnotation as GetViewAction).path
                PostViewAction::class -> RequestMethod.POST to (declaredAnnotation as PostViewAction).path
                PutViewAction::class -> RequestMethod.PUT to (declaredAnnotation as PutViewAction).path
                PatchViewAction::class -> RequestMethod.PATCH to (declaredAnnotation as PatchViewAction).path
                DeleteViewAction::class -> RequestMethod.DELETE to (declaredAnnotation as DeleteViewAction).path
                else -> return@forEach
            }
            val pathMapping = if (viewActionPair.second == "") {
                PathMapping("/$viewComponentName/${method.name}".lowercase(), viewActionPair.first, method)
            } else {
                PathMapping(viewActionPair.second.lowercase(), viewActionPair.first, method)

            }
            createRequestMappingForAnnotation(
                viewComponentName = viewComponentName,
                viewComponentBean = viewComponentBean,
                mapping = pathMapping,
            )
        }
    }

    class PathMapping(
        val path: String,
        val requestMethod: RequestMethod,
        val method: Method
    )

    fun viewActionKey(
        viewComponentName: String,
        viewActionMethodName: String
    ): String {
        return "${viewComponentName}_${viewActionMethodName}".lowercase()
    }
    private fun createRequestMappingForAnnotation(
        viewComponentName: String,
        viewComponentBean: Any,
        mapping: PathMapping,
    ) {
        val options = RequestMappingInfo.BuilderConfiguration();
        val parser = PathPatternParser();
        parser.isCaseSensitive = false
        options.patternParser = parser
        requestMappingHandlerMapping.registerMapping(
            /* mapping = */ RequestMappingInfo.paths(mapping.path)
                .methods(mapping.requestMethod).options(options).build(),
            /* handler = */ viewComponentBean,
            /* method = */ mapping.method
        )
        val key = viewActionKey(viewComponentName, mapping.method.name)
        if (viewActionMapping.containsKey(key)) {
            throw ViewActionConfigurationException("Cannot create duplicate path mapping")
        }
        viewActionMapping[key] = mapping
    }

}
