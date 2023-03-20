package de.tschuehly.thymeleafviewcomponent

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.devtools.filewatch.FileSystemWatcher
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.thymeleaf.dialect.IDialect
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.File
import java.time.Duration


@Configuration
@EnableConfigurationProperties(ViewComponentProperties::class)
@ComponentScan("de.tschuehly.thymeleafviewcomponent")
class ViewComponentAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(SpringTemplateEngine::class)
    fun templateEngine(
        properties: ThymeleafProperties,
        templateResolvers: ObjectProvider<ITemplateResolver>, dialects: ObjectProvider<IDialect>
    ): SpringTemplateEngine? {
        val engine = SpringTemplateEngine()
        engine.enableSpringELCompiler = properties.isEnableSpringElCompiler
        engine.renderHiddenMarkersBeforeCheckboxes = properties.isRenderHiddenMarkersBeforeCheckboxes
        templateResolvers.orderedStream().forEach { templateResolver ->
            engine.addTemplateResolver(
                templateResolver
            )
        }
        dialects.orderedStream().forEach { dialect: IDialect? ->
            engine.addDialect(
                dialect
            )
        }
        engine.addDialect(ViewComponentDialect())
        return engine
    }

    @Bean
    @ConditionalOnProperty("viewcomponent.localDevelopment")
    fun fileViewComponentTemplateResolver(): FileTemplateResolver {
        val fileViewComponentTemplateResolver = FileTemplateResolver()
        if(File("src/main/kotlin").isDirectory){
            fileViewComponentTemplateResolver.prefix = "src/main/kotlin/"
        }
        if(File("src/main/java").isDirectory){
            fileViewComponentTemplateResolver.prefix = "src/main/java/"
        }
        fileViewComponentTemplateResolver.suffix = ".html"
        fileViewComponentTemplateResolver.templateMode = TemplateMode.HTML
        fileViewComponentTemplateResolver.isCacheable = false
        fileViewComponentTemplateResolver.characterEncoding = "UTF-8"
        fileViewComponentTemplateResolver.order = 1
        fileViewComponentTemplateResolver.checkExistence = true
        return fileViewComponentTemplateResolver
    }

    @Bean
    @ConditionalOnProperty("viewcomponent.localDevelopment")
    fun viewComponentFileSystemWatcher(applicationContext: ApplicationContext): FileSystemWatcher {
        val fileSystemWatcher =  FileSystemWatcher(true, Duration.ofMillis(500), Duration.ofMillis(300))
        if(File("src/main/kotlin").isDirectory){
            fileSystemWatcher.addSourceDirectory(File("src/main/kotlin/"))
        }
        if(File("src/main/java").isDirectory){
            fileSystemWatcher.addSourceDirectory(File("src/main/java/"))
        }
        fileSystemWatcher.addListener(ViewComponentChangeListener(applicationContext))
        return fileSystemWatcher
    }
    @Configuration
    @ConditionalOnProperty("viewcomponent.localDevelopment")
    class StartWatcherConfiguration{

        @Autowired
        lateinit var fileSystemWatcher: FileSystemWatcher
        @PostConstruct
        fun startWatcher(){
            fileSystemWatcher.start()
        }

    }

    @Bean
    @ConditionalOnProperty("viewcomponent.localDevelopment", matchIfMissing = true, havingValue = "false")
    fun viewComponentTemplateResolver(): ClassLoaderTemplateResolver {
        val viewComponentTemplateResolver = ClassLoaderTemplateResolver()
        viewComponentTemplateResolver.prefix = ""
        viewComponentTemplateResolver.suffix = ".html"
        viewComponentTemplateResolver.templateMode = TemplateMode.HTML
        viewComponentTemplateResolver.characterEncoding = "UTF-8"
        viewComponentTemplateResolver.order = 1
        viewComponentTemplateResolver.checkExistence = true
        return viewComponentTemplateResolver
    }
}