package de.tschuehly.spring.viewcomponent.jte

import gg.jte.ContentType
import gg.jte.TemplateConfig
import gg.jte.compiler.TemplateCompiler
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.runtime.Constants
import java.nio.file.Path


class JteViewComponentCompiler() {
    fun generate(rootDir: Path, classDirectory: List<String>,packageName: String): String {
        val config = TemplateConfig(
            ContentType.Html,
            Constants.PACKAGE_NAME_PRECOMPILED + packageName
        )
        config.classPath = null
        val compiler = TemplateCompiler(
            /* config = */ config,
            /* codeResolver = */ DirectoryCodeResolver(rootDir),
            /* classDirectory = */ Path.of(classDirectory.first()),
            /* parentClassLoader = */ this.javaClass.classLoader
        )
        return compiler.generateAll().first()
    }
}