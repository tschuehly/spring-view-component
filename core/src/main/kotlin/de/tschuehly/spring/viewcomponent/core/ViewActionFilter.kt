package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class ViewActionFilter(val viewActionConfiguration: ViewActionConfiguration) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val capturingResponseWrapper = CapturingResponseWrapper(response as HttpServletResponse)
        chain.doFilter(request,capturingResponseWrapper)

        val content = capturingResponseWrapper.captureAsString
        val document = Jsoup.parse(content,"", Parser.xmlParser())
        val viewComponentName = capturingResponseWrapper.viewComponentBean?.javaClass?.simpleName?.lowercase()
            ?: throw Error("componentName is null")

        document.firstChild()?.attr("id",viewComponentName)
        val viewActions = document.getElementsByAttribute("view:action")
        viewActions.forEach {el ->
            val methodName = el.attr("view:action")
            el.removeAttr("view:action")
//            TODO: How to get the Information from the ViewAction
            el.attr("hx-post",
                "/$viewComponentName/$methodName")
            el.attr("hx-target","#$viewComponentName")
            el.attr("hx-swap","outerHTML")
        }
        response.writer.write(document.outerHtml())

    }

}