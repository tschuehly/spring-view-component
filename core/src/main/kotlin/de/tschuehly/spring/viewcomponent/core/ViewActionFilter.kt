package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.lang.Boolean
import kotlin.String

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ViewActionFilter(
    private val viewActionParser: ViewActionParser
) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val capturingResponseWrapper = CapturingResponseWrapper(response as HttpServletResponse)
        chain.doFilter(request,capturingResponseWrapper)

        val viewComponentName = capturingResponseWrapper.viewComponentBean?.javaClass?.simpleName?.lowercase()
        val contentType = capturingResponseWrapper.contentType
        val htmlString = capturingResponseWrapper.captureAsString

        if(viewComponentName == null){
            response.writer.write(htmlString)
            return
        }

        if (contentType != null && MediaType.TEXT_HTML.isCompatibleWith(MediaType.parseMediaType(contentType))) {
            val alreadyFilteredAttributeName = getAlreadyFilteredAttributeName()
            val hasAlreadyFilteredAttribute = request.getAttribute(alreadyFilteredAttributeName) != null
            if (!hasAlreadyFilteredAttribute) {
                val parsedHTML = viewActionParser.parseViewComponent(viewComponentName, htmlString)
                request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE)
                response.getWriter().write(parsedHTML)
            } else {
                response.getWriter().write(htmlString)

            }
        } else {
            response.getWriter().write(htmlString)
        }
        return
    }
    protected fun getAlreadyFilteredAttributeName(): String? {
        val name = this.javaClass.name
        return "$name.FILTERED"
    }
}