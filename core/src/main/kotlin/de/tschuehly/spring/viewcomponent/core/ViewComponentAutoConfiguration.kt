package de.tschuehly.spring.viewcomponent.core

import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentChangeListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.devtools.filewatch.FileSystemWatcher
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
@ComponentScan("de.tschuehly.spring.viewcomponent.core")
@EnableConfigurationProperties(ViewComponentProperties::class)
class ViewComponentAutoConfiguration {

    @Configuration
    @ConditionalOnProperty("spring.view-component.local-development")
    class LocalDevConfig {
        val logger: Logger = LoggerFactory.getLogger(LocalDevConfig::class.java)

        @Bean
        fun viewComponentFileSystemWatcher(
            applicationContext: ApplicationContext,
            viewComponentProperties: ViewComponentProperties
        ): FileSystemWatcher {
            val fileSystemWatcher = FileSystemWatcher()
            val viewComponentDirectory = File(viewComponentProperties.viewComponentRoot)
            val templateRoot = File(viewComponentProperties.standaloneTemplateRoot)
            fileSystemWatcher.addSourceDirectory(viewComponentDirectory)
            fileSystemWatcher.addSourceDirectory(templateRoot)
            logger.info("Watching for template changes at: ${viewComponentDirectory.absoluteFile.path}")
            logger.info("Watching for template changes at: ${templateRoot.absoluteFile.path}")
            try {
                fileSystemWatcher.addListener(
                    ViewComponentChangeListener(
                        applicationContext
                    )
                )
            } catch (ex: ClassNotFoundException) {
                logger.error("In order to use hot-reload function, spring dev tools needed", ex)
            } catch (ex: NoClassDefFoundError) {
                logger.error("In order to use hot-reload function, spring dev tools needed", ex)
            }
            fileSystemWatcher.start()
            return fileSystemWatcher
        }
    }

}