package de.tschuehly.spring.viewcomponent.core

import org.springframework.web.bind.annotation.RequestMethod

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostViewAction(
    val path: String = ""
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GetViewAction(
    val path: String = ""
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PutViewAction(
    val path: String = ""
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PatchViewAction(
    val path: String = ""
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeleteViewAction(
    val path: String = ""
)


object ViewActionConstant {
    val attributeName = "view:action"
    val nestedViewComponentAttributeName = "nestedviewcomponent"

}



