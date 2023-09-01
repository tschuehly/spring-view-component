package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.IViewContext

interface ViewContext: IViewContext {
}
class EmptyViewContext(): ViewContext