package de.tschuehly.spring.viewcomponent.core

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewAction(
    val path: String = "",
    val method: RequestMethod = RequestMethod.POST
)
