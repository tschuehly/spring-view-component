package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class ViewActionFilter(val viewActionConfiguration: ViewActionConfiguration) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val capturingResponseWrapper = CapturingResponseWrapper(response as HttpServletResponse)
        chain.doFilter(request,capturingResponseWrapper)

        val viewComponentName = capturingResponseWrapper.viewComponentBean?.javaClass?.simpleName?.lowercase()

        val htmlString = capturingResponseWrapper.captureAsString
        if(viewComponentName == null){
            response.writer.write(htmlString)
            return
        }

        val document = Jsoup.parse(htmlString,"", Parser.xmlParser())

        addHtmxAttrForNestedViewComponents(document)

        processRootElementViewComponent(document,viewComponentName)
        response.writer.write(document.outerHtml())


    }

    private fun processRootElementViewComponent(document: Document, viewComponentName: String) {

        if(document.getElementsByAttribute(ViewAction.attributeName).size == 0){
            // No view:actions in root viewcomponent to process
            return
        }
        val childElement = getSingleChildElement(document)

        if(childElement.nodeName() == "html"){
            // set id on body if it is a whole page that is returned, because htmx cannot swap html element itself
            val bodyElement = document.selectFirst("body")
                ?: throw ViewComponentProcessingException("No body tag in the root ViewComponent found, this is required", null)
            bodyElement.attr("id",viewComponentName)
            replaceViewActionAttrWithHtmxAttr(bodyElement)
        }else{
            childElement.attr("id",viewComponentName)
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
        val nestedViewComponents = document.getElementsByAttribute(ViewAction.nestedViewComponentAttributeName)
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
        val viewActions = viewComponentElement.getElementsByAttribute(ViewAction.attributeName)
        viewActions.forEach { el ->
            val methodName = el.attr(ViewAction.attributeName)
            el.removeAttr(ViewAction.attributeName)
            //            TODO: Here we need to set the HTTP Method from the view:action somehow
            el.attr(
                "hx-post",
                "/$viewComponentName/$methodName"
            )
            el.attr("hx-target", "#$viewComponentName")
            el.attr("hx-swap", "outerHTML")
        }
    }

}