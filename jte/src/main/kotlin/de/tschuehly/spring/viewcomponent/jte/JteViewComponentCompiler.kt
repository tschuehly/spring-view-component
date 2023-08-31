package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentCompiler
import gg.jte.ContentType
import gg.jte.TemplateConfig
import gg.jte.compiler.TemplateCompiler
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.runtime.Constants
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import io.github.classgraph.ScanResult
import java.net.URI
import java.nio.file.Path


class JteViewComponentCompiler() : ViewComponentCompiler {
    override fun compile(rootDir: Path, names: String, classDirectory: List<String>): String {
        val config = TemplateConfig(
            ContentType.Html,
            Constants.PACKAGE_NAME_PRECOMPILED
        )


        config.classPath = classDirectory
        val compiler = TemplateCompiler(
            /* config = */ config,
            /* codeResolver = */ DirectoryCodeResolver(rootDir),
            /* classDirectory = */ Path.of(classDirectory.first()),
            /* parentClassLoader = */ this.javaClass.classLoader
        )
        return compiler.precompile(listOf(names)).first()
    }
}