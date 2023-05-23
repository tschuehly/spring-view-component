package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ViewActionParseInterceptor : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(ViewActionParseInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        response.writer
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        logger.info(response.toString())
        super.afterCompletion(request, response, handler, ex)
    }
}