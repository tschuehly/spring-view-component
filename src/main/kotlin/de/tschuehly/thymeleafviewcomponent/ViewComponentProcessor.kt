package de.tschuehly.thymeleafviewcomponent

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.context.WebEngineContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.context.SpringContextUtils
import org.thymeleaf.standard.expression.StandardExpressions
import org.thymeleaf.templatemode.TemplateMode


class ViewComponentProcessor(dialectPrefix: String) :

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
        val webContext = context as WebEngineContext
        val configuration = webContext.configuration

        val parser = StandardExpressions.getExpressionParser(configuration)
        val (componentName, expressionString) = if (attributeValue.contains(Regex(".render\\(.*\\)"))) {
            Pair(attributeValue.split(".render(")[0], "\${@$attributeValue}")
        } else {
            Pair(attributeValue, "\${@$attributeValue.render()}")
        }
        val appCtx = SpringContextUtils.getApplicationContext(webContext)

        try {
            appCtx.getBean(componentName)
        } catch (e: NoSuchBeanDefinitionException) {
            throw ViewComponentBeanNotFoundException("No ViewComponentBean with the name: \"$componentName\" found")
        }
        val expression = parser.parseExpression(webContext, expressionString)

        val viewContext = expression.execute(webContext) as ViewContext
        val engine = appCtx.getBean(SpringTemplateEngine::class.java)

        webContext.setVariables(viewContext.contextAttributes.toMap())

        val modelFactory = webContext.modelFactory
        val model = modelFactory.createModel().let {
            it.add(
                modelFactory.createText(
                    engine.process(viewContext.componentTemplate, webContext)
                )
            )
            return@let it
        }
        structureHandler.replaceWith(model, false)
    }

}
