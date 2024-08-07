package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.IViewContext
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.ContentCachingResponseWrapper
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.context.WebEngineContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.engine.EngineEventUtils
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import java.nio.charset.StandardCharsets
import java.util.*


class ThymeleafViewComponentTagProcessor(dialectPrefix: String, private val applicationContext: ApplicationContext) :
    AbstractAttributeTagProcessor(
        /* templateMode = */ TemplateMode.HTML,
        /* dialectPrefix = */ dialectPrefix,
        /* elementName = */ null,
        /* prefixElementName = */ false,
        /* attributeName = */ ATTR_NAME,
        /* prefixAttributeName = */ true,
        /* precedence = */ PRECEDENCE,
        /* removeAttribute = */ true
    ) {
    private val logger = LoggerFactory.getLogger(ThymeleafViewComponentTagProcessor::class.java)

    companion object {
        const val ATTR_NAME = "component"
        const val PRECEDENCE = 10000

    }

    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        attributeName: AttributeName,
        attributeValue: String,
        structureHandler: IElementTagStructureHandler
    ) {
        val expression = EngineEventUtils.computeAttributeExpression(context, tag, attributeName, attributeValue)
        val webContext = context as WebEngineContext
        val viewContext = try {
            expression.execute(webContext) as IViewContext
        } catch (e: ClassCastException) {
            throw ViewComponentExpressionException(
                "Could not execute expression: \"${expression.stringRepresentation}\" " +
                        "as ViewContext, did you forget the brackets? \${${expression.stringRepresentation}}, did you pass \"${expression.stringRepresentation}\" as ViewContextProperty?"
            )
        } catch (e: Exception) {
            logger.error("Could not execute expression: \"${expression.stringRepresentation}\"")
            throw ThymeleafViewComponentException(e.message, e.cause)
        }
        val response = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.response!!
        val wrapper = renderViewComponent(response, viewContext, webContext.locale)
        val viewComponentBody = webContext.modelFactory.createText(
            wrapper
        )
        structureHandler.replaceWith(viewComponentBody, true)

    }

    private fun renderViewComponent(
        response: HttpServletResponse,
        viewContext: IViewContext,
        locale: Locale
    ): String {
        val wrapper = ContentCachingResponseWrapper(response)
        val thymeleafViewResolver = applicationContext.getBean(ThymeleafViewResolver::class.java)
        val viewName = thymeleafViewResolver.resolveViewName(
            IViewContext.getViewComponentTemplateWithoutSuffix(viewContext),
            locale
        ) ?: throw ThymeleafViewComponentException("No ViewName", null)
        viewName.render(
            mapOf(viewContext.javaClass.simpleName.replaceFirstChar { it.lowercase() } to viewContext),
            (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request!!,
            wrapper
        )
        return String(wrapper.contentAsByteArray, StandardCharsets.UTF_8)
    }

}
