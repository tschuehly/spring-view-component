package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class ViewActionFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val capturingResponseWrapper = CapturingResponseWrapper(response as HttpServletResponse)
        chain.doFilter(request,capturingResponseWrapper)

        val content = capturingResponseWrapper.captureAsString
        response.writer.write(content)

    }

}