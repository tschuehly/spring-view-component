package de.tschuehly.spring.viewcomponent.core.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher
import org.springframework.boot.devtools.classpath.ClassPathRestartStrategy
import org.springframework.boot.devtools.filewatch.FileSystemWatcherFactory
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

    val kotlinGradleBuildDir = "build/classes/kotlin/main/"
    val javaGradleBuildDir = "build/classes/java/main/"
    val javaMavenBuildDir = "target/classes/"
    val fileSystemWatcher = fileSystemWatcherFactory.fileSystemWatcher
    override fun afterPropertiesSet()  {

        val classPath =
            applicationContext.getBeansWithAnnotation(SpringBootApplication::class.java)
                .values.first().javaClass.protectionDomain.codeSource.location.path
        val (srcDir, buildType) = getSrcDir(classPath)
        fileSystemWatcher.addSourceDirectory(srcDir)
        fileSystemWatcher.addListener(
            ViewComponentChangeListener(
                applicationContext,
                buildType,
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
        if(classPath.endsWith(javaGradleBuildDir)){
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