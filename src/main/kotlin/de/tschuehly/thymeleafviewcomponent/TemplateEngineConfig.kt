package de.tschuehly.thymeleafviewcomponent

import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.File

class TemplateEngineConfig {

    companion object {

        fun templateEngine(clazz: Class<*>): SpringTemplateEngine {
            val templateEngine = SpringTemplateEngine()
            templateEngine.addTemplateResolver(templateResolver(clazz))
            return templateEngine
        }

        private fun templateResolver(clazz: Class<*>): ITemplateResolver {
            val resolver = ClassLoaderTemplateResolver(clazz.classLoader)
            resolver.setPrefix(clazz.`package`.name.replace(".","/") + "/")
            resolver.setSuffix(".html")
            resolver.setTemplateMode("HTML")
            resolver.setOrder(1)
            return resolver
        }
    }
}