package de.tschuehly.spring.viewcomponent.core

import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentFileSystemWatcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher
import org.springframework.boot.devtools.classpath.ClassPathRestartStrategy
import org.springframework.boot.devtools.filewatch.FileSystemWatcherFactory
import org.springframework.boot.devtools.restart.Restarter
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("de.tschuehly.spring.viewcomponent.core")
@EnableConfigurationProperties(ViewComponentProperties::class)
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