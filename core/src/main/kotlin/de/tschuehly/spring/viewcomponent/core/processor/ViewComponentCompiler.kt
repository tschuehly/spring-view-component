package de.tschuehly.spring.viewcomponent.core.processor

import java.nio.file.Path

fun interface ViewComponentCompiler {
    fun compile(rootDir: Path, names: String, classDirectory: List<String>): String
}