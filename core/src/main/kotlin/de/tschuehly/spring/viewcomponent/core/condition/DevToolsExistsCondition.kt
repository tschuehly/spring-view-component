package de.tschuehly.spring.viewcomponent.core.condition

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class DevToolsExistsCondition: Condition {
    private val logger : Logger = LoggerFactory.getLogger(DevToolsExistsCondition::class.java)
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val isLocalDevelopment = context.environment.getProperty("spring.view-component.local-development").toBoolean()
        val isDevToolsExists = try {
            Class.forName("org.springframework.boot.devtools.restart.Restarter", false, context.classLoader)
            true
        } catch (e: ClassNotFoundException) {
            logger.error("In order to use hot-reload function, spring-dev-tools needed")
            false
        }

        return isLocalDevelopment && isDevToolsExists
    }
}