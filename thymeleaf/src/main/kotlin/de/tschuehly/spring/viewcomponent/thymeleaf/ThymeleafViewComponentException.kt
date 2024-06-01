package de.tschuehly.spring.viewcomponent.thymeleaf

class ThymeleafViewComponentException(override val message: String?, override val cause: Throwable?) : Exception(message) {
}