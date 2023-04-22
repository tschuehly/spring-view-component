package de.tschuehly.spring.viewcomponent.core

import org.springframework.boot.devtools.classpath.ClassPathChangedEvent
import org.springframework.boot.devtools.filewatch.ChangedFiles
import org.springframework.boot.devtools.filewatch.FileChangeListener
import org.springframework.context.ApplicationEventPublisher

class ViewComponentChangeListener(
    private val eventPublisher: ApplicationEventPublisher
) : FileChangeListener {
    override fun onChange(changeSet: MutableSet<ChangedFiles>) {
        if (changeSet.any { changedFiles ->
                changedFiles.files.any {
                    it.relativeName.endsWith(".html") || it.relativeName.endsWith(".jte")
                }
            }) {
            publishEvent(ClassPathChangedEvent(this, changeSet, false))
        }
    }

    private fun publishEvent(event: ClassPathChangedEvent) {
        this.eventPublisher.publishEvent(event)
    }
}