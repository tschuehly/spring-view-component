package de.tschuehly.spring.viewcomponent.core.processor

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher
import org.springframework.boot.devtools.classpath.ClassPathRestartStrategy
import org.springframework.boot.devtools.filewatch.FileSystemWatcher
import org.springframework.boot.devtools.filewatch.FileSystemWatcherFactory
import org.springframework.context.ApplicationContext
import java.io.File
import java.net.URL


class ViewComponentFileSystemWatcher(
    private val applicationContext: ApplicationContext,
    fileSystemWatcherFactory: FileSystemWatcherFactory,
    restartStrategy: ClassPathRestartStrategy,
    urls: Array<out URL>
) : ClassPathFileSystemWatcher(
    fileSystemWatcherFactory, restartStrategy, urls
) {

    private val logger: Logger = LoggerFactory.getLogger(ViewComponentFileSystemWatcher::class.java)
    private val fileSystemWatcher: FileSystemWatcher = fileSystemWatcherFactory.fileSystemWatcher
    override fun afterPropertiesSet() {
        val classPathList = this.applicationContext.getBeansWithAnnotation(ViewComponent::class.java).values.map {
            it.javaClass.protectionDomain.codeSource.location.path
        }.toSet()
        classPathList.forEach { classPath ->
            val srcDir = File(classPath)
            fileSystemWatcher.addSourceDirectory(srcDir)
            logger.info("Watching for template changes at: ${srcDir.absoluteFile.path}")
        }
        fileSystemWatcher.addListener(
            ViewComponentChangeListener(
                applicationContext
            )
        )
        fileSystemWatcher.start()
    }
}