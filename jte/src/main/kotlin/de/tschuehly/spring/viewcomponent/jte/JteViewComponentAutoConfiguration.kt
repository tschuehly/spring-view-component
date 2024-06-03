package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateConfig
import gg.jte.TemplateEngine
import gg.jte.compiler.TemplateCompiler
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.resolve.ResourceCodeResolver
import gg.jte.runtime.Constants
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
@Import(ViewComponentAutoConfiguration::class, JteConfiguration::class)
class JteViewComponentAutoConfiguration {

}