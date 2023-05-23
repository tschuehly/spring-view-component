package de.tschuehly.spring.viewcomponent.core

interface IViewContext {
    var componentBean: Any?
    var componentTemplate: String?
    val contextAttributes: Array<out ViewProperty>
}