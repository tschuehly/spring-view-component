package de.tschuehly.spring.viewcomponent.core.processor

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher
import org.springframework.boot.devtools.classpath.ClassPathRestartStrategy
import org.springframework.boot.devtools.filewatch.FileSystemWatcherFactory
import org.springframework.boot.devtools.livereload.LiveReloadServer
import org.springframework.context.ApplicationContext
import java.io.File
import java.net.URL


class ViewComponentFileSystemWatcher(
    private val applicationContext: ApplicationContext,
    private val fileSystemWatcherFactory: FileSystemWatcherFactory,
    private val restartStrategy: ClassPathRestartStrategy,
    private val urls: Array<out URL>
) : ClassPathFileSystemWatcher(
    fileSystemWatcherFactory, restartStrategy, urls,
) {

    val logger = LoggerFactory.getLogger(ViewComponentFileSystemWatcher::class.java)

    val kotlinGradleBuildDir = "build/classes/kotlin/main/"
    val javaGradleBuildDir = "build/classes/java/main/"
    val javaMavenBuildDir = "target/classes/"
    val fileSystemWatcher = fileSystemWatcherFactory.fileSystemWatcher
    override fun afterPropertiesSet() {
        val classPathList = this.applicationContext.getBeansWithAnnotation(ViewComponent::class.java).values.map {
            it.javaClass.protectionDomain.codeSource.location.path
        }.toSet()
        var buildType: ViewComponentParser.BuildType? = null
         classPathList.forEach{ classPath ->
            val (srcDir, classPathBuildType) = getSrcDir(classPath)
            fileSystemWatcher.addSourceDirectory(srcDir)
            logger.info("Watching for template changes at: ${srcDir.absoluteFile.path}")
             buildType = classPathBuildType
        }
        fileSystemWatcher.addListener(
            ViewComponentChangeListener(
                applicationContext,
                buildType ?: throw ViewComponentException("BuildType could not be determined"),
                applicationContext

            )
        )
        fileSystemWatcher.start()
    }

    private fun getSrcDir(classPath: String): Pair<File, ViewComponentParser.BuildType> {
        if (classPath.endsWith(kotlinGradleBuildDir)) {
            val srcDir = classPath.split(kotlinGradleBuildDir)[0] + "/src/main/kotlin"
            val file = File(srcDir)
            if (file.exists()) {
                return file to ViewComponentParser.BuildType.GRADLE
            }
        }
        if (classPath.endsWith(javaGradleBuildDir)) {
            val srcDir = classPath.split(javaGradleBuildDir)[0] + "src/main/java"
            val file = File(srcDir)
            if (file.exists()) {
                return file to ViewComponentParser.BuildType.GRADLE
            }
        }
        if (classPath.endsWith(javaMavenBuildDir)) {
            val srcDir = classPath.split(javaMavenBuildDir)[0] + "src/main/java"
            val file = File(srcDir)
            if (file.exists()) {
                return file to ViewComponentParser.BuildType.MAVEN
            }
        }
        throw ViewComponentProcessingException("No srcDir found", null)
    }
}