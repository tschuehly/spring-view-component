package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class ViewActionParseInterceptor : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(ViewActionParseInterceptor::class.java)

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        super.postHandle(request, response, handler, modelAndView)
    }
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if(response is CapturingResponseWrapper){
            val captureResponse: CapturingResponseWrapper = response
            captureResponse.viewComponentBean
        }
        super.afterCompletion(request, response, handler, ex)
    }
}