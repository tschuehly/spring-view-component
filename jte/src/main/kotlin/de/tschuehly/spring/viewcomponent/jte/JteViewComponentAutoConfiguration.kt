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

@PropertySource("jte.properties")
@Import(ViewComponentAutoConfiguration::class)
class JteViewComponentAutoConfiguration {
    @Bean
    fun jteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): JteViewContextAspect {
        return JteViewContextAspect(templateEngine, jteProperties)
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine::class)
    fun jteTemplateEngine(viewComponentProperties: ViewComponentProperties,applicationContext: ApplicationContext): TemplateEngine {
        if (!viewComponentProperties.localDevelopment) {
            val viewComponentTemplateList = applicationContext.getBeansWithAnnotation(ViewComponent::class.java).values.map {
                getViewComponentReference(it)
            }.map { reference ->
                reference.replace(".", "/") + ".jte"
            }
            val config = TemplateConfig(
                ContentType.Html,
                Constants.PACKAGE_NAME_PRECOMPILED
            )
            config.classPath = null
            val compiler = TemplateCompiler(
                /* config = */ config,
                /* codeResolver = */ ResourceCodeResolver(""),
                /* classDirectory = */ Path.of(this.javaClass.classLoader.getResource("").toURI()),
                /* parentClassLoader = */ this.javaClass.classLoader
            )
            val compiledTemplates = compiler.precompile(viewComponentTemplateList)
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(
                /* classDirectory = */ Path.of(this.javaClass.classLoader.getResource("").toURI()),
                /* contentType = */ ContentType.Html,
                /* parentClassLoader = */ this.javaClass.classLoader
            )
        }
        val split = viewComponentProperties.viewComponentRoot.split("/").toTypedArray()
        val codeResolver: CodeResolver = DirectoryCodeResolver(FileSystems.getDefault().getPath("", *split))
        return TemplateEngine.create(
            codeResolver,
            Paths.get("jte-classes"),
            ContentType.Html,
            javaClass.classLoader
        )
    }

    private fun getViewComponentReference(viewComponentClass: Any): String {
        val javaClass = if (AopUtils.isAopProxy(viewComponentClass) && viewComponentClass is Advised) {
            viewComponentClass.targetSource.target!!.javaClass
        } else viewComponentClass.javaClass

        return javaClass.packageName + "." +javaClass.simpleName
    }


    @Bean
    @Primary
    fun jteProperties(viewComponentProperties: ViewComponentProperties) = JteProperties().also {
        it.templateSuffix = viewComponentProperties.templateSuffix
        it.templateLocation = viewComponentProperties.viewComponentRoot
        it.isDevelopmentMode = viewComponentProperties.localDevelopment
    }
}