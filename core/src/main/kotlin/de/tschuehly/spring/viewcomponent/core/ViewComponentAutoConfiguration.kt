package de.tschuehly.spring.viewcomponent.core

import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentFileSystemWatcher
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentParser.BuildType
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentProcessingException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher
import org.springframework.boot.devtools.classpath.ClassPathRestartStrategy
import org.springframework.boot.devtools.filewatch.FileSystemWatcherFactory
import org.springframework.boot.devtools.restart.Restarter
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
@ComponentScan("de.tschuehly.spring.viewcomponent.core")
class ViewComponentAutoConfiguration {

    @Configuration
    @ConditionalOnProperty("spring.view-component.local-development")
    class LocalDevConfig(
        private val classPathRestartStrategy: ClassPathRestartStrategy
    ) {
        @Bean
        fun viewComponentFileSystemWatcher(
            applicationContext: ApplicationContext,
            fileSystemWatcherFactory: FileSystemWatcherFactory,
            restartStrategy: ClassPathRestartStrategy,
            applicationEventPublisher: ApplicationEventPublisher
        ): ClassPathFileSystemWatcher {
            val urls = Restarter.getInstance().initialUrls ?: arrayOf()
            val watcher = ViewComponentFileSystemWatcher(
                applicationContext,
                fileSystemWatcherFactory,
                classPathRestartStrategy,
                urls
            )
            watcher.setStopWatcherOnRestart(true)
            return watcher
        }
    }
}