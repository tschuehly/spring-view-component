package de.tschuehly.spring.viewcomponent.core.processor

import de.tschuehly.spring.viewcomponent.core.action.*
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.boot.devtools.classpath.ClassPathChangedEvent
import org.springframework.boot.devtools.filewatch.ChangedFiles
import org.springframework.boot.devtools.filewatch.FileChangeListener
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.ContextRefreshedEvent


class ViewComponentChangeListener(
    private val applicationContext: ApplicationContext,
    private val buildType: ViewComponentParser.BuildType,
    private val applicationEventPublisher: ApplicationEventPublisher
) : FileChangeListener {
    private val logger = LoggerFactory.getLogger(ViewComponentChangeListener::class.java)
    override fun onChange(changeSet: MutableSet<ChangedFiles>) {

        if (
            isTemplate(changeSet)
        ) {
            val srcFile = changeSet.first().files.first().file
            val bean = applicationContext.getBeansWithAnnotation(ViewComponent::class.java).filter {
                it.key.lowercase() == srcFile.nameWithoutExtension.lowercase()
            }.values.first()
            val javaClass = if (AopUtils.isAopProxy(bean) && bean is Advised) {
                bean.targetSource.target!!.javaClass
            } else bean.javaClass
            val methodList = getViewActionMethods(javaClass)
            logger.debug("Detected Change to {}", srcFile.absoluteFile)
            val parser = ViewComponentParser(
                srcFile = srcFile,
                buildType = buildType,
                methodList = methodList,
                viewComponentName = javaClass.simpleName.lowercase()
            )
            parser.parseFile(true)
//            applicationEventPublisher.publishEvent(ContextRefreshedEvent(applicationContext))
//            applicationEventPublisher.publishEvent(ClassPathChangedEvent(this, changeSet, false))
//            if(srcFile.extension == "kte" || srcFile.extension == "jte"){
//                // TODO: if restart is set to false then update resources in intelliJ triggers livereload
//                applicationEventPublisher.publishEvent(ClassPathChangedEvent(this, changeSet, false))
//            }else{
//                applicationEventPublisher.publishEvent(ClassPathChangedEvent(this, changeSet, false))
//            }
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

    private fun getViewActionMethods(javaClass: Class<Any>) =
        javaClass.declaredMethods.mapNotNull { method ->
            if (method.getAnnotation(GetViewAction::class.java) != null) {
                val get = (method.getAnnotation(GetViewAction::class.java) as GetViewAction)
                return@mapNotNull ViewActionMethod(method.name.toString(), get.path, GetViewAction::class.java)
            }
            if (method.getAnnotation(PostViewAction::class.java) != null) {
                val get = (method.getAnnotation(PostViewAction::class.java) as PostViewAction)
                return@mapNotNull ViewActionMethod(method.name.toString(), get.path, PostViewAction::class.java)
            }
            if (method.getAnnotation(PutViewAction::class.java) != null) {
                val get = (method.getAnnotation(PutViewAction::class.java) as PutViewAction)
                return@mapNotNull ViewActionMethod(method.name.toString(), get.path, PutViewAction::class.java)
            }
            if (method.getAnnotation(PatchViewAction::class.java) != null) {
                val get = (method.getAnnotation(PatchViewAction::class.java) as PatchViewAction)
                return@mapNotNull ViewActionMethod(method.name.toString(), get.path, PatchViewAction::class.java)
            }
            if (method.getAnnotation(DeleteViewAction::class.java) != null) {
                val get = (method.getAnnotation(DeleteViewAction::class.java) as DeleteViewAction)
                return@mapNotNull ViewActionMethod(method.name.toString(), get.path, DeleteViewAction::class.java)
            }
            return@mapNotNull null
        }

}