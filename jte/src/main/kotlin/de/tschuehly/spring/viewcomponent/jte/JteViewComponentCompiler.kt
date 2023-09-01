package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentCompiler
import gg.jte.ContentType
import gg.jte.TemplateConfig
import gg.jte.compiler.TemplateCompiler
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.runtime.Constants
import java.nio.file.Path


class JteViewComponentCompiler() : ViewComponentCompiler {
    override fun generate(rootDir: Path, names: String, classDirectory: List<String>,packageName: String): String {
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

    override fun compile(rootDir: Path, names: String, classDirectory: List<String>,packageName: String): String {
        val config = TemplateConfig(
            ContentType.Html,
            Constants.PACKAGE_NAME_PRECOMPILED +packageName
        )


        config.classPath = null
        val compiler = TemplateCompiler(
            /* config = */ config,
            /* codeResolver = */ DirectoryCodeResolver(rootDir),
            /* classDirectory = */ Path.of(classDirectory.first()),
            /* parentClassLoader = */ this.javaClass.classLoader
        )
        return compiler.precompile(listOf(names)).first()
    }
}