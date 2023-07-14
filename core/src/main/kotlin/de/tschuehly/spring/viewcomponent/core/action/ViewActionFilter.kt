package de.tschuehly.spring.viewcomponent.core.action

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ViewActionFilter(
    private val viewActionParser: ViewActionParser
) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val capturingResponseWrapper = CapturingResponseWrapper(response as HttpServletResponse)
        chain.doFilter(request,capturingResponseWrapper)
        val viewComponentName = capturingResponseWrapper.viewComponentBean?.javaClass?.simpleName?.lowercase()
        val htmlString = capturingResponseWrapper.captureAsString
        if(viewComponentName == null){
            response.writer.write(htmlString)
            return
        }
        val parsedHTML = viewActionParser.parseViewComponent(viewComponentName, htmlString)
        response.writer.write(parsedHTML)
    }
}