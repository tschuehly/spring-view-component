package de.tschuehly.spring.viewcomponent.core.action

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.lang.reflect.Method
import de.tschuehly.spring.viewcomponent.core.action.ViewActionRegistry.PathMapping
import org.slf4j.LoggerFactory

@Configuration
class ViewActionConfiguration(
    val context: ApplicationContext,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    val requestMappingHandlerMapping: RequestMappingHandlerMapping,
    val viewActionRegistry: ViewActionRegistry
) {

    @PostConstruct
    fun registerViewActionEndpoints() {
        val viewComponentBeans = context.getBeansWithAnnotation(ViewComponent::class.java)

        viewComponentBeans.forEach { (_, viewComponentBean) ->
            val beanType = ClassUtils.getUserClass(viewComponentBean.javaClass)
            val viewComponentName = beanType.simpleName
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
                PathMapping(
                    "/$viewComponentName/${method.name}".lowercase(),
                    viewActionPair.first,
                    method
                )
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
        viewActionRegistry.registerMapping(viewComponentName, mapping)
    }

}
