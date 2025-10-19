package de.tschuehly.spring.viewcomponent.core.component

import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

@Component
class ViewComponentMvcConfigurer(
    private val methodReturnValueHandlers: List<HandlerMethodReturnValueHandler>,
    private val applicationContext: ApplicationContext
) : WebMvcConfigurer {

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        methodReturnValueHandlers.forEach {
            handlers.add(it)
        }
        super.addReturnValueHandlers(handlers)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val viewComponentBeans = applicationContext.getBeansWithAnnotation(ViewComponent::class.java)
        viewComponentBeans
            .map { (_, viewComponent) ->
                viewComponent.javaClass.`package`.name.replace(".", "/")
            }.toSet()
            .forEach { path ->
                registry.addResourceHandler(
                    "/view-src/$path/**"
                ).addResourceLocations("classpath:/$path/")
                    .resourceChain(true)
                    .addResolver(object : PathResourceResolver() {
                        override fun getResource(resourcePath: String, location: Resource): Resource? {
                            // Only serve allowed extensions
                            if (resourcePath.matches(".*\\.(html|jte|jpg|css|js|png|svg|webp)$".toRegex())) {
                                return super.getResource(resourcePath, location)
                            }
                            return null
                        }
                    })
            }

        super.addResourceHandlers(registry)
    }

}
