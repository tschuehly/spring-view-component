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
        if(response is CapturingResponseWrapper){
            val captureResponse: CapturingResponseWrapper = response
            captureResponse.viewComponentBean = modelAndView?.modelMap?.get("viewComponentBean")
            super.postHandle(request, captureResponse, handler, modelAndView)
        }
        super.postHandle(request, response, handler, modelAndView)
    }
}