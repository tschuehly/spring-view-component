package de.tschuehly.spring.viewcomponent.core

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMethod

@Service
class ViewActionParser(val viewActionConfiguration: ViewActionConfiguration) {
    fun parseViewComponent(viewComponentName: String, htmlString: String): String {

        val document = Jsoup.parse(htmlString, "", Parser.xmlParser())

        addHtmxAttrForNestedViewComponents(document)

        processRootElementViewComponent(document, viewComponentName)
        return document.outerHtml()
    }

    private fun processRootElementViewComponent(document: Document, viewComponentName: String) {

        if (document.getElementsByAttribute(ViewActionConstant.attributeName).size == 0) {
            // No view:actions in root viewcomponent to process
            return
        }
        val childElement = getSingleChildElement(document)

        if (childElement.nodeName() == "html") {
            // set id on body if it is a whole page that is returned, because htmx cannot swap html element itself
            val bodyElement = document.selectFirst("body")
                ?: throw ViewComponentProcessingException(
                    "No body tag in the root ViewComponent found, this is required",
                    null
                )
            bodyElement.attr("id", viewComponentName)
            replaceViewActionAttrWithHtmxAttr(bodyElement)
        } else {
            childElement.attr("id", viewComponentName)
            replaceViewActionAttrWithHtmxAttr(childElement)
        }
    }

    private fun getSingleChildElement(document: Document): Element {
        val firstChild = document.firstElementChild()
        if (document.lastElementChild() != firstChild || firstChild == null) throw ViewComponentProcessingException(
            "ViewComponent need to have one root html node",
            null
        )
        return firstChild
    }

    private fun addHtmxAttrForNestedViewComponents(document: Element) {
        val nestedViewComponents = document.getElementsByAttribute(ViewActionConstant.nestedViewComponentAttributeName)
        nestedViewComponents.forEach { viewComponentElement ->
            replaceViewActionAttrWithHtmxAttr(viewComponentElement)
        }
    }

    private fun replaceViewActionAttrWithHtmxAttr(
        viewComponentElement: Element,
    ) {
        val viewComponentName = viewComponentElement.id()
        if (viewComponentName == "") throw ViewComponentProcessingException(
            "Id with name of viewcomponent is needed on the viewcomponent element, this should be set " +
                    "by the templating engine when processing nested viewcomponents", null
        )
        val postViewActions = viewComponentElement.getElementsByAttribute(ViewActionConstant.attributeName)
        postViewActions.forEach { el ->
            val splitMethodAttribute = el.attr(ViewActionConstant.attributeName).split("?")
            val methodName = splitMethodAttribute[0]
            el.removeAttr(ViewActionConstant.attributeName)
            val viewActionKey = viewActionConfiguration.viewActionKey(viewComponentName, methodName)
            val viewActionMapping = viewActionConfiguration.viewActionMapping[viewActionKey]
                ?: throw ViewComponentProcessingException(
                    "ViewActionMapping with the key $viewActionKey not found",
                    null
                )
            val path = splitMethodAttribute.getOrNull(1)?.let { pathAttributeString ->
                "${viewActionMapping.path}?$pathAttributeString"
            } ?: viewActionMapping.path

            el.attr(
                getHXAttr(viewActionMapping.requestMethod),
                path
            )
            if (el.attr("hx-target") == "") {
                el.attr("hx-target", "#$viewComponentName")
            }
            if (el.attr("hx-swap") == "") {
                el.attr("hx-swap", "outerHTML")
            }
        }
    }

    fun getHXAttr(requestMethod: RequestMethod): String {
        return when (requestMethod) {
            RequestMethod.GET -> "hx-get"
            RequestMethod.POST -> "hx-post"
            RequestMethod.PUT -> "hx-put"
            RequestMethod.PATCH -> "hx-patch"
            RequestMethod.DELETE -> "hx-delete"
            else -> throw ViewComponentProcessingException("RequestMethod: ${requestMethod.name} not supported", null)
        }
    }
}