package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentUtils
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentProcessingException
import org.slf4j.LoggerFactory
import org.thymeleaf.context.Context
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.context.WebEngineContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.engine.EngineEventUtils
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.context.SpringContextUtils
import org.thymeleaf.templatemode.TemplateMode


class ThymeleafViewComponentTagProcessor(dialectPrefix: String) :
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
        val ATTR_NAME = "component"
        val PRECEDENCE = 10000

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
            throw ViewComponentProcessingException(e.message, e.cause)
        }
        val appCtx = SpringContextUtils.getApplicationContext(webContext)
        val engine = appCtx.getBean(SpringTemplateEngine::class.java)

        val context = Context()
        context.setVariable(
            viewContext.javaClass.simpleName.replaceFirstChar { it.lowercase() },
            viewContext
        )
        var sp = SpringContextUtils.getRequestContext(webContext)
        val context2 = SpringContextUtils.getRequestContext(webContext)
            context2.model[viewContext.javaClass.simpleName.replaceFirstChar { it.lowercase() }] = viewContext

        webContext.setVariable(viewContext.javaClass.simpleName.replaceFirstChar { it.lowercase() }, viewContext)

        // TODO: Cannot process attribute '{th:field,data-th-field}': no associated BindStatus could be found for the intended form binding operations. This can be due to the lack of a proper management of the Spring RequestContext, which is usually done through the ThymeleafView or ThymeleafReactiveView (template:
        val modelFactory = webContext.modelFactory
        engine.enableSpringELCompiler = true
        val viewComponentBody = modelFactory.createText(
            engine.process(IViewContext.getViewComponentTemplate(viewContext), webContext)
        )
        structureHandler.setAttribute("id", ViewComponentUtils.getId(viewContext.javaClass))
        structureHandler.removeAttribute("view:component")
        structureHandler.setBody(viewComponentBody, true)
    }

}
