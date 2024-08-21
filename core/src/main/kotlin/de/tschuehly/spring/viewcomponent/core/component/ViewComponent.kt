package de.tschuehly.spring.viewcomponent.core.component

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ViewComponent(@get:AliasFor(annotation = Component::class, attribute = "value") val value: String = "")

