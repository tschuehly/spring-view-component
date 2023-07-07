package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.ViewActionConstant
import de.tschuehly.spring.viewcomponent.core.ViewComponentProcessingException
import de.tschuehly.spring.viewcomponent.core.toMap
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.context.WebEngineContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.engine.EngineEventUtils
import org.thymeleaf.exceptions.TemplateProcessingException
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.context.SpringContextUtils
import org.thymeleaf.templatemode.TemplateMode


class ThymeleafViewComponentProcessor(dialectPrefix: String) :

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
        } catch (e: TemplateProcessingException) {
            throw ViewComponentProcessingException(e.message, e.cause)
        }
        val viewComponentName = viewContext.componentBean?.javaClass?.simpleName?.lowercase()
            ?: throw ViewComponentProcessingException(
                "viewContext.componentBean of expression $attributeValue is somehow null",
                null
            )
        val appCtx = SpringContextUtils.getApplicationContext(webContext)
        val engine = appCtx.getBean(SpringTemplateEngine::class.java)
        SpringContextUtils.getRequestContext(webContext).model.putAll(viewContext.contextAttributes.toMap())
        webContext.setVariables(viewContext.contextAttributes.toMap())

        val modelFactory = webContext.modelFactory
        val model = modelFactory.createModel().let { model ->
            model.add(
                modelFactory.createOpenElementTag(
                    "div",
                    mapOf(
                        "id" to viewComponentName,
                        ViewActionConstant.nestedViewComponentAttributeName to ""
                    ),
                    AttributeValueQuotes.DOUBLE,
                    false
                )
            )
            model.add(
                modelFactory.createText(
                    engine.process(viewContext.componentTemplate, webContext)
                )
            )
            model.add(modelFactory.createCloseElementTag("div"))
            return@let model
        }
        structureHandler.replaceWith(model, false)
    }

}
