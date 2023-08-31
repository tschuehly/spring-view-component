package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.ViewActionConstant
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentProcessingException
import de.tschuehly.spring.viewcomponent.core.toMap
import org.slf4j.LoggerFactory
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
        SpringContextUtils.getRequestContext(webContext).model.putAll(IViewContext.getAttributes(viewContext))
        webContext.setVariables(IViewContext.getAttributes(viewContext))

        val componentName = viewContext.javaClass.enclosingClass.simpleName.substringBefore("$$")
        val componentPackage = viewContext.javaClass.enclosingClass.`package`.name.replace(".", "/") + "/"

        val modelFactory = webContext.modelFactory
        val viewComponentBody = modelFactory.createText(
            engine.process("$componentPackage$componentName", webContext)
        )

        structureHandler.setAttribute("id", viewContext.javaClass.enclosingClass.simpleName.lowercase())
        structureHandler.setAttribute(ViewActionConstant.nestedViewComponentAttributeName, null)
        structureHandler.removeAttribute("view:component")
        structureHandler.setBody(viewComponentBody, true)
    }

}
