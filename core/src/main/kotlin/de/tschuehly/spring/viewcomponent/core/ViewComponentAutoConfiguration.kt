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
        private val classPathRestartStrategy: ClassPathRestartStrategy,
        private val eventPublisher: ApplicationEventPublisher
    ) {
        val gradleKotlinBuildDir = "build/classes/kotlin/main/"
        val javaMavenBuildDir = "target/classes/"
        private val logger = LoggerFactory.getLogger(LocalDevConfig::class.java)

        @Bean
        fun viewComponentFileSystemWatcher(
            applicationContext: ApplicationContext,
            fileSystemWatcherFactory: FileSystemWatcherFactory,
            restartStrategy: ClassPathRestartStrategy,
             applicationEventPublisher: ApplicationEventPublisher
        ): ClassPathFileSystemWatcher{
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

//        @Bean
//        fun viewComponentFileSystemWatcher(applicationContext: ApplicationContext): FileSystemWatcher {
//            val fileSystemWatcher = FileSystemWatcher(true, Duration.ofMillis(300), Duration.ofMillis(200))
//            val classPath =
//                applicationContext.getBeansWithAnnotation(SpringBootApplication::class.java)
//                    .values.first().javaClass.protectionDomain.codeSource.location.path
//            val (srcDir, buildType) = getSrcDir(classPath)
//            logger.info("Registering fileSystemWatcher: ${srcDir.path}")
//            fileSystemWatcher.addSourceDirectory(srcDir)
//            fileSystemWatcher.addListener(
//                ViewComponentChangeListener(
//                    applicationContext,
//                    buildType
//                )
//            )
//            fileSystemWatcher.start()
//            return fileSystemWatcher
//        }

        private fun getSrcDir(classPath: String): Pair<File, BuildType> {
            if (classPath.endsWith(gradleKotlinBuildDir)) {
                val srcDir = classPath.split(gradleKotlinBuildDir)[0] + "/src/main/kotlin"
                val file = File(srcDir)
                if (file.exists()) {
                    return file to BuildType.GRADLE
                }
            }
            if (classPath.endsWith(javaMavenBuildDir)) {
                val srcDir = classPath.split(javaMavenBuildDir)[0] + "src/main/java"
                val file = File(srcDir)
                if (file.exists()) {
                    return file to BuildType.MAVEN
                }
            }
            throw ViewComponentProcessingException("No srcDir found", null)
        }
    }

}