package de.tschuehly.spring.viewcomponent.thymeleaf

import org.slf4j.LoggerFactory
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class ThymeleafViewComponentSrcAttributeProcessor(
    dialectPrefix: String
) : AbstractAttributeTagProcessor(
    /* templateMode = */ TemplateMode.HTML,
    /* dialectPrefix = */ dialectPrefix,
    /* elementName = */ null,
    /* prefixElementName = */ false,
    /* attributeName = */ "src", // attribute name: view:src
    /* prefixAttributeName = */ true,
    /* precedence = */ 1000,
    /* removeAttribute = */ true
) {
    private val logger = LoggerFactory.getLogger(ThymeleafViewComponentSrcAttributeProcessor::class.java)

    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        attributeName: AttributeName,
        attributeValue: String,
        structureHandler: IElementTagStructureHandler
    ) {
        try {
            // Determine template path as Thymeleaf knows it.
            // templateData.template typically contains the template name/path used by the engine.
            val templateName =
                context.templateData.template
            val dir = templateName.substringBeforeLast('/', "")           // directory portion or "" if none

            // Build classpath resource path relative to the template directory
            val resourcePath = if (dir.isEmpty()) {
                "/view-src/$attributeValue"
            } else {
                "/view-src/$dir/$attributeValue"
            }

            // Set or replace src attribute with the data URI and remove our custom attribute
            structureHandler.setAttribute("src", resourcePath)
            structureHandler.removeAttribute(attributeName)
        } catch (ex: Exception) {
            logger.error("Failed inlining image for attribute value '$attributeValue'", ex)
            // best-effort: remove our attribute so rendering continues
            structureHandler.removeAttribute(attributeName)
        }
    }
}