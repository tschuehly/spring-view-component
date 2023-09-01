package de.tschuehly.spring.viewcomponent.core.processor

import java.nio.file.Path

interface ViewComponentCompiler {
    fun generate(rootDir: Path, names: String, classDirectory: List<String>, packageName: String): String
    fun compile(rootDir: Path, names: String, classDirectory: List<String>, packageName: String): String
}