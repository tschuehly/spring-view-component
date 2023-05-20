package de.tschuehly.spring.viewcomponent.core

interface IViewContext {
    var componentTemplate: String?
    val contextAttributes: Array<out ViewProperty>
}