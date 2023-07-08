package de.tschuehly.spring.viewcomponent.core

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import java.lang.reflect.Method

@Component
class ViewActionRegistry {
    private val viewActionMapping = mutableMapOf<String, PathMapping>()

    fun registerMapping(viewComponentName: String, mapping: PathMapping) {
        val key = viewActionKey(viewComponentName, mapping.method.name)
        if (viewActionMapping.containsKey(key)) {
            throw ViewActionRegistryException("Cannot register duplicate path mapping")
        }
        viewActionMapping[key] = mapping
    }

    fun getMapping(viewComponentName: String,
                   viewActionMethodName: String): PathMapping {
        val viewActionKey = viewActionKey(viewComponentName, viewActionMethodName)
        return viewActionMapping[viewActionKey]
            ?: throw ViewActionRegistryException(
                "ViewActionMapping with the key $viewActionKey not found"
            )
    }

    fun viewActionKey(
        viewComponentName: String,
        viewActionMethodName: String
    ): String {
        return "${viewComponentName}_${viewActionMethodName}".lowercase()
    }

    class PathMapping(
        val path: String,
        val requestMethod: RequestMethod,
        val method: Method
    )
}