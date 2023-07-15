package de.tschuehly.spring.viewcomponent.core

import de.tschuehly.spring.viewcomponent.core.component.ViewComponentChangeListener
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.devtools.filewatch.FileSystemWatcher
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.io.File
import java.time.Duration

@Configuration
@ComponentScan("de.tschuehly.spring.viewcomponent.core.component")
class ViewComponentAutoConfiguration(
) {


    @ConditionalOnProperty("spring.view-component.view-action.enabled", havingValue = "true")
    @ComponentScan("de.tschuehly.spring.viewcomponent.core.action")
    class ViewActionConfiguration {}

    @Bean
    @ConditionalOnProperty("spring.view-component.local-development")
    fun viewComponentFileSystemWatcher(applicationContext: ApplicationContext): FileSystemWatcher {
        val fileSystemWatcher = FileSystemWatcher(true, Duration.ofMillis(500), Duration.ofMillis(300))
        if (File("src/main/kotlin").isDirectory) {
            fileSystemWatcher.addSourceDirectory(File("src/main/kotlin/"))
        }
        if (File("src/main/java").isDirectory) {
            fileSystemWatcher.addSourceDirectory(File("src/main/java/"))
        }
        fileSystemWatcher.addListener(ViewComponentChangeListener(applicationContext))
        return fileSystemWatcher
    }

    @Configuration
    @ConditionalOnProperty("viewcomponent.localDevelopment")
    class StartWatcherConfiguration(
        val fileSystemWatcher: FileSystemWatcher
    ) {
        @PostConstruct
        fun startWatcher() {
            fileSystemWatcher.start()
        }

    }
}