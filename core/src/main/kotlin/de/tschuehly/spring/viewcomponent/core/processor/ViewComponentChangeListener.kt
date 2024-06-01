package de.tschuehly.spring.viewcomponent.core.processor

import org.slf4j.LoggerFactory
import org.springframework.boot.devtools.filewatch.ChangedFiles
import org.springframework.boot.devtools.filewatch.FileChangeListener
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent


class ViewComponentChangeListener(
    private val applicationContext: ApplicationContext
) : FileChangeListener {
    private val logger = LoggerFactory.getLogger(ViewComponentChangeListener::class.java)
    override fun onChange(changeSet: MutableSet<ChangedFiles>) {

        if (
            isTemplate(changeSet)
        ) {
            val srcFile = changeSet.first().files.first().file
            logger.debug("Detected Change to {}", srcFile.absoluteFile)
            applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))
        }
    }

    private fun isTemplate(changeSet: MutableSet<ChangedFiles>) =
        changeSet.any { changedFiles ->
            changedFiles.files.any {
                it.relativeName.endsWith(".html") ||
                        it.relativeName.endsWith(".th") ||
                        it.relativeName.endsWith(".jte") ||
                        it.relativeName.endsWith(".kte")
            }
        }


}